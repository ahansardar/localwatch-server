package com.localwatch.server.updater

import android.content.Context
import com.localwatch.server.BuildConfig
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import java.util.Locale

internal object UpdateRepository {
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 30_000

    fun configuredRepo(): String? {
        val configured = BuildConfig.GITHUB_REPO.trim().trimEnd('/')
        if (configured.isBlank()) return null
        val path = if (configured.startsWith("http://") || configured.startsWith("https://")) {
            runCatching { URI(configured).path }.getOrNull().orEmpty()
        } else {
            configured
        }
        val parts = path.trim('/').split('/').filter { it.isNotBlank() }
        val repoIndex = parts.indexOf("repos")
        return when {
            repoIndex >= 0 && parts.size > repoIndex + 2 ->
                "${parts[repoIndex + 1]}/${parts[repoIndex + 2]}"
            parts.size >= 2 -> "${parts[0]}/${parts[1]}"
            else -> null
        }
    }

    fun fetchLatestRelease(): ReleaseInfo {
        val repo = configuredRepo()
            ?: error("Set LOCALWATCH_GITHUB_REPO to owner/repository or its GitHub releases URL.")
        val connection = open("https://api.github.com/repos/$repo/releases/latest")
        val responseCode = connection.responseCode
        val response = (if (responseCode in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()
        connection.disconnect()
        if (responseCode !in 200..299) {
            error("GitHub release check failed (HTTP $responseCode).")
        }
        val release = JSONObject(response)
        val assets = release.getJSONArray("assets")
        val apkAsset = (0 until assets.length())
            .map { assets.getJSONObject(it) }
            .firstOrNull {
                it.optString("name").endsWith(".apk", ignoreCase = true) ||
                    it.optString("content_type") == "application/vnd.android.package-archive"
            }
            ?: error("The latest GitHub release does not contain an APK asset.")
        return ReleaseInfo(
            tag = release.getString("tag_name"),
            title = release.optString("name").ifBlank { release.getString("tag_name") },
            markdownBody = release.optString("body"),
            apkUrl = apkAsset.getString("browser_download_url"),
            apkName = apkAsset.getString("name"),
            apkSize = apkAsset.optLong("size", -1L),
            sha256 = apkAsset.optString("digest")
                .removePrefix("sha256:")
                .takeIf { it.matches(Regex("[a-fA-F0-9]{64}")) },
        )
    }

    fun isNewer(tag: String): Boolean {
        val latest = versionParts(tag)
        val current = versionParts(BuildConfig.VERSION_NAME)
        if (latest.isEmpty()) return false
        val length = maxOf(latest.size, current.size)
        for (index in 0 until length) {
            val latestPart = latest.getOrElse(index) { 0 }
            val currentPart = current.getOrElse(index) { 0 }
            if (latestPart != currentPart) return latestPart > currentPart
        }
        return false
    }

    fun download(
        context: Context,
        release: ReleaseInfo,
        onProgress: (downloaded: Long, total: Long) -> Unit,
    ): File {
        val directory = File(context.cacheDir, "updates").apply { mkdirs() }
        directory.listFiles()?.forEach { if (it.name != release.apkName) it.delete() }
        val destination = File(directory, release.apkName)
        val temporary = File(directory, "${release.apkName}.part")
        val connection = open(release.apkUrl)
        connection.instanceFollowRedirects = true
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            connection.disconnect()
            error("APK download failed (HTTP $responseCode).")
        }
        val total = connection.contentLengthLong.takeIf { it > 0 } ?: release.apkSize
        val digest = MessageDigest.getInstance("SHA-256")
        var downloaded = 0L
        connection.inputStream.use { input ->
            FileOutputStream(temporary).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                    digest.update(buffer, 0, count)
                    downloaded += count
                    onProgress(downloaded, total)
                }
                output.fd.sync()
            }
        }
        connection.disconnect()
        if (total > 0 && downloaded != total) {
            temporary.delete()
            error("The APK download was incomplete.")
        }
        release.sha256?.let { expected ->
            val actual = digest.digest().joinToString("") {
                "%02x".format(Locale.US, it.toInt() and 0xff)
            }
            if (!actual.equals(expected, ignoreCase = true)) {
                temporary.delete()
                error("The downloaded APK failed SHA-256 verification.")
            }
        }
        destination.delete()
        if (!temporary.renameTo(destination)) {
            temporary.copyTo(destination, overwrite = true)
            temporary.delete()
        }
        return destination
    }

    private fun versionParts(version: String): List<Int> =
        Regex("\\d+").findAll(version.substringBefore('-')).map { it.value.toInt() }.toList()

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "LocalWatch/${BuildConfig.VERSION_NAME}")
        }
}
