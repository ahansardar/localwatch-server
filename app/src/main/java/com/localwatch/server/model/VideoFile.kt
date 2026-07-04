package com.localwatch.server.model

import android.net.Uri

data class VideoFile(
    val id: String,
    val name: String,
    val uri: Uri,
    val size: Long,
    val mimeType: String,
    val modifiedAt: Long,
)

data class ClientInfo(
    val address: String,
    val lastSeenAt: Long,
)

data class ServerUiState(
    val running: Boolean = false,
    val starting: Boolean = false,
    val url: String? = null,
    val error: String? = null,
    val scanning: Boolean = false,
    val videos: List<VideoFile> = emptyList(),
    val clients: List<ClientInfo> = emptyList(),
)
