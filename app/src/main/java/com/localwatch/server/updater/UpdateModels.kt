package com.localwatch.server.updater

import org.json.JSONObject

data class ReleaseInfo(
    val tag: String,
    val title: String,
    val markdownBody: String,
    val apkUrl: String,
    val apkName: String,
    val apkSize: Long,
    val sha256: String?,
) {
    fun toJson(): String = JSONObject()
        .put("tag", tag)
        .put("title", title)
        .put("body", markdownBody)
        .put("apkUrl", apkUrl)
        .put("apkName", apkName)
        .put("apkSize", apkSize)
        .put("sha256", sha256)
        .toString()

    companion object {
        fun fromJson(value: String): ReleaseInfo {
            val json = JSONObject(value)
            return ReleaseInfo(
                tag = json.getString("tag"),
                title = json.optString("title").ifBlank { json.getString("tag") },
                markdownBody = json.optString("body"),
                apkUrl = json.getString("apkUrl"),
                apkName = json.getString("apkName"),
                apkSize = json.optLong("apkSize", -1L),
                sha256 = json.optString("sha256").takeIf { it.isNotBlank() && it != "null" },
            )
        }
    }
}

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val release: ReleaseInfo) : UpdateState
    data class Downloading(val release: ReleaseInfo, val downloaded: Long, val total: Long) : UpdateState
    data class NeedsInstallPermission(val release: ReleaseInfo, val apkPath: String) : UpdateState
    data class Installing(val release: ReleaseInfo) : UpdateState
    data class Error(val message: String, val release: ReleaseInfo? = null) : UpdateState
}
