package com.localwatch.server.server

import android.content.Context
import com.localwatch.server.data.AppSettings
import com.localwatch.server.model.VideoFile
import com.localwatch.server.web.WebTemplates
import fi.iki.elonen.NanoHTTPD
import java.io.InputStream
import java.net.URLEncoder
import java.util.Collections
import java.util.UUID

class LocalHttpServer(
    private val context: Context,
    port: Int,
    private val videos: () -> List<VideoFile>,
    private val settings: () -> AppSettings,
    private val onClientSeen: (String) -> Unit,
) : NanoHTTPD(null, port) {

    private val sessions = Collections.synchronizedSet(mutableSetOf<String>())

    override fun serve(request: IHTTPSession): Response {
        onClientSeen(request.remoteIpAddress ?: "Unknown device")
        return runCatching { route(request) }.getOrElse {
            html(
                Response.Status.INTERNAL_ERROR,
                WebTemplates.error("Something went wrong", "The host could not complete this request.", 500)
            )
        }.also { response ->
            if (request.uri != "/assets/logo.png") {
                response.addHeader("Cache-Control", "no-store")
            }
            response.addHeader("X-Content-Type-Options", "nosniff")
            response.addHeader("Referrer-Policy", "no-referrer")
        }
    }

    private fun route(request: IHTTPSession): Response {
        val appSettings = settings()
        if (request.uri == "/assets/logo.png") {
            return newChunkedResponse(
                Response.Status.OK,
                "image/png",
                context.assets.open("localwatch-logo.png")
            ).also {
                it.addHeader("Cache-Control", "public, max-age=86400")
            }
        }
        if (appSettings.requirePin && !isAuthorized(request)) {
            return when {
                request.uri == "/auth" && request.method == Method.POST -> authenticate(request, appSettings.pin)
                request.uri == "/" -> html(Response.Status.UNAUTHORIZED, WebTemplates.pin())
                else -> html(
                    Response.Status.UNAUTHORIZED,
                    WebTemplates.error("Access denied", "Enter the watch party PIN first.", 401)
                )
            }
        }

        return when {
            request.uri == "/" -> html(
                Response.Status.OK,
                WebTemplates.library(android.os.Build.MODEL.ifBlank { "Android host" }, appSettings.allowDownloads)
            )
            request.uri == "/auth" -> redirect("/")
            request.uri == "/api/status" -> json(
                """{"running":true,"videoCount":${videos().size},"downloadsEnabled":${appSettings.allowDownloads}}"""
            )
            request.uri == "/api/videos" -> json(videoJson(videos()))
            request.uri == "/watch" -> {
                val video = findVideo(request) ?: return notFound()
                html(
                    Response.Status.OK,
                    WebTemplates.player(
                        name = video.name,
                        id = video.id,
                        mime = video.mimeType,
                        size = video.size,
                        modifiedAt = video.modifiedAt,
                        downloads = appSettings.allowDownloads
                    )
                )
            }
            request.uri == "/stream" -> {
                val video = findVideo(request) ?: return notFound()
                stream(request, video, attachment = false)
            }
            request.uri == "/download" -> {
                if (!appSettings.allowDownloads) {
                    html(Response.Status.FORBIDDEN, WebTemplates.error("Downloads disabled", "The host has disabled downloads for this session.", 403))
                } else {
                    val video = findVideo(request) ?: return notFound()
                    stream(request, video, attachment = true)
                }
            }
            else -> notFound()
        }
    }

    private fun authenticate(request: IHTTPSession, expectedPin: String): Response {
        val files = mutableMapOf<String, String>()
        runCatching { request.parseBody(files) }
        val submitted = request.parameters["pin"]?.firstOrNull().orEmpty()
        if (expectedPin.isBlank() || submitted != expectedPin) {
            return html(Response.Status.UNAUTHORIZED, WebTemplates.pin(error = true))
        }
        val token = UUID.randomUUID().toString()
        sessions += token
        return redirect("/").also {
            it.addHeader("Set-Cookie", "lw_session=$token; Path=/; HttpOnly; SameSite=Strict")
        }
    }

    private fun isAuthorized(request: IHTTPSession): Boolean {
        val cookie = request.headers["cookie"].orEmpty()
        val token = cookie.split(';')
            .map { it.trim() }
            .firstOrNull { it.startsWith("lw_session=") }
            ?.substringAfter('=')
        return token != null && token in sessions
    }

    private fun findVideo(request: IHTTPSession): VideoFile? {
        val id = request.parameters["id"]?.firstOrNull() ?: return null
        return videos().firstOrNull { it.id == id }
    }

    private fun stream(request: IHTTPSession, video: VideoFile, attachment: Boolean): Response {
        val total = video.size
        if (total <= 0) return html(
            Response.Status.NOT_FOUND,
            WebTemplates.error("Video unavailable", "This file is empty or no longer readable.", 404)
        )

        val parsedRange = RangeRequestHandler.parse(request.headers["range"], total)
        if (parsedRange is RangeResult.Invalid) {
            return newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "").also {
                it.addHeader("Content-Range", "bytes */$total")
            }
        }

        val range = (parsedRange as? RangeResult.Partial)?.range
        val start = range?.start ?: 0L
        val end = range?.end ?: total - 1
        val length = end - start + 1
        val source = context.contentResolver.openInputStream(video.uri)
            ?: return notFound()
        skipFully(source, start)
        val body = BoundedInputStream(source, length)
        val status = if (range != null) Response.Status.PARTIAL_CONTENT else Response.Status.OK
        return newFixedLengthResponse(status, video.mimeType, body, length).also {
            it.addHeader("Accept-Ranges", "bytes")
            it.addHeader("Content-Length", length.toString())
            if (range != null) it.addHeader("Content-Range", "bytes $start-$end/$total")
            if (attachment) {
                val encoded = URLEncoder.encode(video.name, "UTF-8").replace("+", "%20")
                it.addHeader("Content-Disposition", "attachment; filename*=UTF-8''$encoded")
            }
        }
    }

    private fun skipFully(input: InputStream, count: Long) {
        var remaining = count
        while (remaining > 0) {
            val skipped = input.skip(remaining)
            if (skipped > 0) remaining -= skipped
            else if (input.read() < 0) error("Unexpected end of video")
            else remaining--
        }
    }

    private fun videoJson(items: List<VideoFile>): String = buildString {
        append("""{"videos":[""")
        items.forEachIndexed { index, item ->
            if (index > 0) append(',')
            append("""{"id":"${jsonEscape(item.id)}","name":"${jsonEscape(item.name)}","size":${item.size},"mimeType":"${jsonEscape(item.mimeType)}","modifiedAt":${item.modifiedAt}}""")
        }
        append("]}")
    }

    private fun jsonEscape(value: String): String = buildString {
        value.forEach {
            when (it) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (it.code < 0x20) append("\\u%04x".format(it.code)) else append(it)
            }
        }
    }

    private fun notFound() = html(
        Response.Status.NOT_FOUND,
        WebTemplates.error("Not found", "That video or page is no longer available.", 404)
    )

    private fun html(status: Response.Status, content: String) =
        newFixedLengthResponse(status, "text/html; charset=utf-8", content)

    private fun json(content: String) =
        newFixedLengthResponse(Response.Status.OK, "application/json; charset=utf-8", content)

    private fun redirect(location: String) =
        newFixedLengthResponse(Response.Status.REDIRECT, MIME_HTML, "").also { it.addHeader("Location", location) }
}
