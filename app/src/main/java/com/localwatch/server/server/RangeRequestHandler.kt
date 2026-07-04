package com.localwatch.server.server

data class ByteRange(val start: Long, val end: Long, val total: Long) {
    val length: Long get() = end - start + 1
    val contentRange: String get() = "bytes $start-$end/$total"
}

sealed interface RangeResult {
    data object Full : RangeResult
    data class Partial(val range: ByteRange) : RangeResult
    data object Invalid : RangeResult
}

object RangeRequestHandler {
    fun parse(header: String?, total: Long): RangeResult {
        if (header == null) return RangeResult.Full
        if (total <= 0 || !header.startsWith("bytes=", ignoreCase = true) || header.contains(',')) {
            return RangeResult.Invalid
        }
        val parts = header.substringAfter('=').trim().split('-', limit = 2)
        if (parts.size != 2) return RangeResult.Invalid

        val range = if (parts[0].isBlank()) {
            val suffix = parts[1].toLongOrNull()?.coerceAtMost(total) ?: return RangeResult.Invalid
            if (suffix <= 0) return RangeResult.Invalid
            ByteRange(total - suffix, total - 1, total)
        } else {
            val start = parts[0].toLongOrNull() ?: return RangeResult.Invalid
            val requestedEnd = parts[1].toLongOrNull() ?: (total - 1)
            if (start < 0 || start >= total || requestedEnd < start) return RangeResult.Invalid
            ByteRange(start, minOf(requestedEnd, total - 1), total)
        }
        return RangeResult.Partial(range)
    }
}
