package com.localwatch.server.server

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RangeRequestHandlerTest {
    @Test
    fun noRangeRequestsWholeFile() {
        assertTrue(RangeRequestHandler.parse(null, 1_000) is RangeResult.Full)
    }

    @Test
    fun parsesExplicitRange() {
        val result = RangeRequestHandler.parse("bytes=100-299", 1_000) as RangeResult.Partial
        assertEquals(ByteRange(100, 299, 1_000), result.range)
        assertEquals(200, result.range.length)
        assertEquals("bytes 100-299/1000", result.range.contentRange)
    }

    @Test
    fun clampsEndToFileLength() {
        val result = RangeRequestHandler.parse("bytes=900-2000", 1_000) as RangeResult.Partial
        assertEquals(ByteRange(900, 999, 1_000), result.range)
    }

    @Test
    fun parsesOpenEndedRange() {
        val result = RangeRequestHandler.parse("bytes=750-", 1_000) as RangeResult.Partial
        assertEquals(ByteRange(750, 999, 1_000), result.range)
    }

    @Test
    fun parsesSuffixRange() {
        val result = RangeRequestHandler.parse("bytes=-250", 1_000) as RangeResult.Partial
        assertEquals(ByteRange(750, 999, 1_000), result.range)
    }

    @Test
    fun rejectsUnsatisfiableAndMultipartRanges() {
        assertTrue(RangeRequestHandler.parse("bytes=1000-", 1_000) is RangeResult.Invalid)
        assertTrue(RangeRequestHandler.parse("bytes=0-1,5-6", 1_000) is RangeResult.Invalid)
        assertTrue(RangeRequestHandler.parse("wat", 1_000) is RangeResult.Invalid)
    }
}
