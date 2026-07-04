package com.localwatch.server.updater

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.localwatch.server.MainActivity
import com.localwatch.server.R

class UpdateCheckWorker(
    appContext: Context,
    params: WorkerParameters,
) : Worker(appContext, params) {
    override fun doWork(): Result {
        if (UpdateRepository.configuredRepo() == null) return Result.success()
        return runCatching {
            val release = UpdateRepository.fetchLatestRelease()
            applicationContext.getSharedPreferences("localwatch_updates", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_checked", System.currentTimeMillis())
                .apply()
            if (UpdateRepository.isNewer(release.tag)) {
                UpdateManager.publishBackgroundRelease(applicationContext, release)
                showNotification(release)
            }
            Result.success()
        }.getOrElse { Result.success() }
    }

    private fun showNotification(release: ReleaseInfo) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "LocalWatch updates", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val openApp = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("LocalWatch ${release.tag} is available")
                .setContentText("Tap to review the release notes and update.")
                .setStyle(NotificationCompat.BigTextStyle().bigText(release.title))
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build()
        )
    }

    companion object {
        private const val CHANNEL_ID = "localwatch_updates"
        private const val NOTIFICATION_ID = 8081
    }
}
