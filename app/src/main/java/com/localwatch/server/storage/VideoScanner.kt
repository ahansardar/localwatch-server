package com.localwatch.server.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.localwatch.server.model.VideoFile
import java.security.MessageDigest
import java.util.Locale

class VideoScanner(private val context: Context) {
    private val extensions = setOf("mp4", "mkv", "webm", "avi", "mov", "m4v")

    fun scan(treeUri: Uri): List<VideoFile> {
        val root = DocumentFile.fromTreeUri(context, treeUri)
            ?: throw IllegalStateException("The selected folder is no longer available.")
        if (!root.canRead()) {
            throw SecurityException("Folder permission was lost. Please choose the folder again.")
        }

        val results = mutableListOf<VideoFile>()
        val pending = ArrayDeque<DocumentFile>()
        pending.add(root)

        while (pending.isNotEmpty()) {
            val folder = pending.removeFirst()
            folder.listFiles().forEach { file ->
                when {
                    file.isDirectory -> pending.add(file)
                    file.isFile && isVideo(file) -> results += VideoFile(
                        id = stableId(file.uri),
                        name = file.name ?: "Untitled video",
                        uri = file.uri,
                        size = file.length(),
                        mimeType = resolveMime(file),
                        modifiedAt = file.lastModified(),
                    )
                }
            }
        }
        return results.sortedBy { it.name.lowercase(Locale.ROOT) }
    }

    private fun isVideo(file: DocumentFile): Boolean {
        val extension = file.name?.substringAfterLast('.', "")?.lowercase(Locale.ROOT)
        return file.type?.startsWith("video/") == true || extension in extensions
    }

    private fun resolveMime(file: DocumentFile): String {
        if (file.type?.startsWith("video/") == true) return file.type!!
        return when (file.name?.substringAfterLast('.', "")?.lowercase(Locale.ROOT)) {
            "mp4", "m4v" -> "video/mp4"
            "webm" -> "video/webm"
            "mkv" -> "video/x-matroska"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            else -> "application/octet-stream"
        }
    }

    private fun stableId(uri: Uri): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(uri.toString().toByteArray())
        return bytes.take(12).joinToString("") { "%02x".format(it) }
    }
}
