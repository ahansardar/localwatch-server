package com.localwatch.server.data

import android.content.Context
import android.net.Uri

data class AppSettings(
    val folderUri: Uri? = null,
    val folderName: String = "",
    val port: Int = 8080,
    val allowDownloads: Boolean = true,
    val requirePin: Boolean = false,
    val pin: String = "1234",
    val keepAwake: Boolean = true,
    val darkMode: Boolean = true,
    val showDeviceNames: Boolean = true,
    val accentColor: Int = 0xFF9CF0FF.toInt(),
)

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("localwatch_settings", Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        folderUri = prefs.getString(KEY_FOLDER_URI, null)?.let(Uri::parse),
        folderName = prefs.getString(KEY_FOLDER_NAME, "") ?: "",
        port = prefs.getInt(KEY_PORT, 8080).coerceIn(1024, 65535),
        allowDownloads = prefs.getBoolean(KEY_DOWNLOADS, true),
        requirePin = prefs.getBoolean(KEY_REQUIRE_PIN, false),
        pin = prefs.getString(KEY_PIN, "1234") ?: "1234",
        keepAwake = prefs.getBoolean(KEY_KEEP_AWAKE, true),
        darkMode = prefs.getBoolean(KEY_DARK_MODE, true),
        showDeviceNames = prefs.getBoolean(KEY_SHOW_DEVICE_NAMES, true),
        accentColor = prefs.getInt(KEY_ACCENT_COLOR, 0xFF9CF0FF.toInt()),
    )

    fun save(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_FOLDER_URI, settings.folderUri?.toString())
            .putString(KEY_FOLDER_NAME, settings.folderName)
            .putInt(KEY_PORT, settings.port)
            .putBoolean(KEY_DOWNLOADS, settings.allowDownloads)
            .putBoolean(KEY_REQUIRE_PIN, settings.requirePin)
            .putString(KEY_PIN, settings.pin)
            .putBoolean(KEY_KEEP_AWAKE, settings.keepAwake)
            .putBoolean(KEY_DARK_MODE, settings.darkMode)
            .putBoolean(KEY_SHOW_DEVICE_NAMES, settings.showDeviceNames)
            .putInt(KEY_ACCENT_COLOR, settings.accentColor)
            .apply()
    }

    companion object {
        private const val KEY_FOLDER_URI = "folder_uri"
        private const val KEY_FOLDER_NAME = "folder_name"
        private const val KEY_PORT = "port"
        private const val KEY_DOWNLOADS = "downloads"
        private const val KEY_REQUIRE_PIN = "require_pin"
        private const val KEY_PIN = "pin"
        private const val KEY_KEEP_AWAKE = "keep_awake"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_SHOW_DEVICE_NAMES = "show_device_names"
        private const val KEY_ACCENT_COLOR = "accent_color"
    }
}
