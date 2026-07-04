package com.localwatch.server.web

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebTemplatesTest {
    @Test
    fun libraryIsOfflineResponsiveAndUsesRealActions() {
        val html = WebTemplates.library("Test host", downloads = true)

        assertTrue(html.contains("/assets/logo.png"))
        assertTrue(html.contains("/api/videos"))
        assertTrue(html.contains("id=\"search\""))
        assertTrue(html.contains("id=\"sort\""))
        assertTrue(html.contains("id=\"refresh\""))
        assertTrue(html.contains("/download?id="))
        assertTrue(html.contains("@media (orientation: landscape)"))
        assertFalse(html.contains("fonts.googleapis.com"))
        assertFalse(html.contains("cdn.tailwindcss.com"))
        assertFalse(html.contains("href=\"#\""))
    }

    @Test
    fun downloadsDisappearWhenHostDisablesThem() {
        val library = WebTemplates.library("Test host", downloads = false)
        val player = WebTemplates.player(
            name = "Movie.mp4",
            id = "safe-id",
            mime = "video/mp4",
            size = 1024,
            modifiedAt = 1,
            downloads = false
        )

        assertTrue(library.contains("const downloadsEnabled = false;"))
        assertFalse(player.contains("href=\"/download?id="))
        assertTrue(player.contains("Ask the host to enable downloads"))
    }

    @Test
    fun playerAndPinControlsAreConnected() {
        val player = WebTemplates.player(
            name = "Movie.mp4",
            id = "safe-id",
            mime = "video/mp4",
            size = 2048,
            modifiedAt = 1,
            downloads = true
        )
        val pin = WebTemplates.pin(error = true)

        assertTrue(player.contains("id=\"speed\""))
        assertTrue(player.contains("id=\"fullscreen\""))
        assertTrue(player.contains("id=\"share\""))
        assertTrue(player.contains("playbackRate"))
        assertTrue(pin.contains("method=\"post\" action=\"/auth\""))
        assertTrue(pin.contains("name=\"pin\""))
        assertTrue(pin.contains("id=\"pin-toggle\""))
        assertTrue(pin.contains("That PIN did not match"))
    }
}
