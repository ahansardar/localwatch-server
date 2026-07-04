package com.localwatch.server.updater

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

object UpdateManager {
    private const val PREFS = "localwatch_updates"
    private const val KEY_PENDING_RELEASE = "pending_release"
    private const val KEY_LAST_CHECKED = "last_checked"
    private const val KEY_LAST_PROMPTED = "last_prompted"
    private const val KEY_DOWNLOADED_APK = "downloaded_apk"
    private const val DAILY_MS = 24L * 60L * 60L * 1000L
    private const val UNIQUE_WORK = "localwatch_daily_update_check"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = mutableState.asStateFlow()
    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (appContext == null) appContext = context.applicationContext
        if (UpdateRepository.configuredRepo() == null) return
        scheduleDaily(context)
        val prefs = prefs(context)
        val storedPending = prefs.getString(KEY_PENDING_RELEASE, null)
            ?.let { runCatching { ReleaseInfo.fromJson(it) }.getOrNull() }
        val pending = storedPending?.takeIf { UpdateRepository.isNewer(it.tag) }
        if (storedPending != null && pending == null) clearPending(context)
        val downloadedPath = prefs.getString(KEY_DOWNLOADED_APK, null)
        if (pending != null && downloadedPath != null && File(downloadedPath).isFile) {
            mutableState.value = UpdateState.NeedsInstallPermission(pending, downloadedPath)
        } else if (
            pending != null &&
            System.currentTimeMillis() - prefs.getLong(KEY_LAST_PROMPTED, 0L) >= DAILY_MS
        ) {
            mutableState.value = UpdateState.Available(pending)
        }
        if (System.currentTimeMillis() - prefs.getLong(KEY_LAST_CHECKED, 0L) >= DAILY_MS) {
            checkNow(silentWhenCurrent = true)
        }
    }

    fun checkNow(silentWhenCurrent: Boolean = false) {
        val context = appContext ?: return
        if (UpdateRepository.configuredRepo() == null) {
            mutableState.value = UpdateState.Error(
                "Set LOCALWATCH_GITHUB_REPO before building to enable updates."
            )
            return
        }
        mutableState.value = UpdateState.Checking
        scope.launch {
            runCatching { UpdateRepository.fetchLatestRelease() }
                .onSuccess { release ->
                    prefs(context).edit().putLong(KEY_LAST_CHECKED, System.currentTimeMillis()).apply()
                    if (UpdateRepository.isNewer(release.tag)) {
                        savePending(context, release)
                        mutableState.value = UpdateState.Available(release)
                    } else {
                        clearPending(context)
                        mutableState.value = if (silentWhenCurrent) UpdateState.Idle else UpdateState.UpToDate
                    }
                }
                .onFailure {
                    mutableState.value = if (silentWhenCurrent) {
                        UpdateState.Idle
                    } else {
                        UpdateState.Error(it.message ?: "Unable to check for updates.")
                    }
                }
        }
    }

    fun downloadAndInstall(release: ReleaseInfo) {
        val context = appContext ?: return
        scope.launch {
            runCatching {
                UpdateRepository.download(context, release) { downloaded, total ->
                    mutableState.value = UpdateState.Downloading(release, downloaded, total)
                }.also { validateApk(context, it) }
            }.onSuccess { file ->
                prefs(context).edit().putString(KEY_DOWNLOADED_APK, file.absolutePath).apply()
                installOrRequestPermission(context, release, file)
            }.onFailure {
                mutableState.value = UpdateState.Error(it.message ?: "Unable to download the update.", release)
            }
        }
    }

    fun dismiss() {
        val context = appContext ?: return
        val editor = prefs(context).edit().putLong(KEY_LAST_PROMPTED, System.currentTimeMillis())
        if (mutableState.value is UpdateState.NeedsInstallPermission) {
            val path = (mutableState.value as UpdateState.NeedsInstallPermission).apkPath
            File(path).delete()
            editor.remove(KEY_DOWNLOADED_APK)
        }
        editor.apply()
        mutableState.value = UpdateState.Idle
    }

    fun openInstallPermission(context: Context) {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
        )
    }

    fun onActivityResumed(context: Context) {
        val current = mutableState.value
        if (
            current is UpdateState.NeedsInstallPermission &&
            context.packageManager.canRequestPackageInstalls()
        ) {
            val file = File(current.apkPath)
            if (file.isFile) {
                scope.launch { commitInstall(context.applicationContext, current.release, file) }
            }
        }
    }

    internal fun publishBackgroundRelease(context: Context, release: ReleaseInfo) {
        savePending(context, release)
        if (mutableState.value is UpdateState.Idle) {
            mutableState.value = UpdateState.Available(release)
        }
    }

    internal fun reportInstallError(message: String) {
        mutableState.value = UpdateState.Error(message)
    }

    private fun installOrRequestPermission(context: Context, release: ReleaseInfo, file: File) {
        if (!context.packageManager.canRequestPackageInstalls()) {
            mutableState.value = UpdateState.NeedsInstallPermission(release, file.absolutePath)
            return
        }
        commitInstall(context, release, file)
    }

    private fun commitInstall(context: Context, release: ReleaseInfo, file: File) {
        mutableState.value = UpdateState.Installing(release)
        runCatching {
            val installer = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(context.packageName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
            }
            val sessionId = installer.createSession(params)
            installer.openSession(sessionId).use { session ->
                session.openWrite("LocalWatch.apk", 0, file.length()).use { output ->
                    FileInputStream(file).use { input -> input.copyTo(output) }
                    session.fsync(output)
                }
                val resultIntent = Intent(context, UpdateInstallReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    sessionId,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                session.commit(pendingIntent.intentSender)
            }
        }.onFailure {
            mutableState.value = UpdateState.Error(it.message ?: "Unable to start the Android installer.", release)
        }
    }

    @Suppress("DEPRECATION")
    private fun validateApk(context: Context, file: File) {
        val archive = context.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
            ?: error("Android could not parse the downloaded APK.")
        if (archive.packageName != context.packageName) {
            file.delete()
            error("The downloaded APK belongs to a different application.")
        }
        val archiveVersion = if (Build.VERSION.SDK_INT >= 28) archive.longVersionCode else archive.versionCode.toLong()
        val installed = context.packageManager.getPackageInfo(context.packageName, 0)
        val installedVersion = if (Build.VERSION.SDK_INT >= 28) installed.longVersionCode else installed.versionCode.toLong()
        if (archiveVersion <= installedVersion) {
            file.delete()
            error("The downloaded APK does not have a higher versionCode.")
        }
    }

    private fun scheduleDaily(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun savePending(context: Context, release: ReleaseInfo) {
        prefs(context).edit().putString(KEY_PENDING_RELEASE, release.toJson()).apply()
    }

    private fun clearPending(context: Context) {
        prefs(context).edit()
            .remove(KEY_PENDING_RELEASE)
            .remove(KEY_DOWNLOADED_APK)
            .apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
