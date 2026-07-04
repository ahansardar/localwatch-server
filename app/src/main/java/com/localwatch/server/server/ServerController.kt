package com.localwatch.server.server

import android.content.Context
import com.localwatch.server.data.AppSettings
import com.localwatch.server.data.SettingsManager
import com.localwatch.server.model.ClientInfo
import com.localwatch.server.model.ServerUiState
import com.localwatch.server.network.NetworkUtils
import com.localwatch.server.storage.VideoScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

object ServerController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(ServerUiState())
    val state: StateFlow<ServerUiState> = mutableState.asStateFlow()
    private val clients = ConcurrentHashMap<String, Long>()
    private var server: LocalHttpServer? = null
    private lateinit var appContext: Context
    private lateinit var settingsManager: SettingsManager
    @Volatile private var settings = AppSettings()

    fun initialize(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        settingsManager = SettingsManager(appContext)
        settings = settingsManager.load()
        refreshLibrary()
    }

    fun currentSettings(): AppSettings = settings

    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
        settingsManager.save(newSettings)
    }

    fun refreshLibrary() {
        if (!::appContext.isInitialized) return
        val folder = settings.folderUri
        if (folder == null) {
            mutableState.value = mutableState.value.copy(videos = emptyList(), scanning = false)
            return
        }
        scope.launch {
            mutableState.value = mutableState.value.copy(scanning = true, error = null)
            runCatching {
                withContext(Dispatchers.IO) { VideoScanner(appContext).scan(folder) }
            }.onSuccess { videos ->
                mutableState.value = mutableState.value.copy(videos = videos, scanning = false)
            }.onFailure { error ->
                mutableState.value = mutableState.value.copy(
                    scanning = false,
                    error = error.message ?: "Could not scan the selected folder."
                )
            }
        }
    }

    fun start(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        if (server != null) {
            onComplete(true, null)
            return
        }
        mutableState.value = mutableState.value.copy(starting = true, error = null)
        scope.launch {
            if (settings.folderUri == null) {
                mutableState.value = mutableState.value.copy(starting = false, error = "Choose a video folder first.")
                onComplete(false, "Choose a video folder first.")
                return@launch
            }
            if (settings.requirePin && settings.pin.isBlank()) {
                val message = "Enter a viewer PIN before starting the protected server."
                mutableState.value = mutableState.value.copy(starting = false, error = message)
                onComplete(false, message)
                return@launch
            }
            val url = NetworkUtils.serverUrl(settings.port)
            if (url == null) {
                val message = "No local network found. Turn on Wi-Fi or hotspot, then try again."
                mutableState.value = mutableState.value.copy(starting = false, error = message)
                onComplete(false, message)
                return@launch
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    LocalHttpServer(
                        context = appContext,
                        port = settings.port,
                        videos = { mutableState.value.videos },
                        settings = { settings },
                        onClientSeen = ::recordClient,
                    ).also { it.start(5_000, false) }
                }
            }.onSuccess {
                server = it
                mutableState.value = mutableState.value.copy(running = true, starting = false, url = url, error = null)
                onComplete(true, null)
            }.onFailure {
                val message = it.message ?: "The server could not start. Try another port."
                mutableState.value = mutableState.value.copy(running = false, starting = false, error = message)
                onComplete(false, message)
            }
        }
    }

    fun stop() {
        server?.stop()
        server = null
        clients.clear()
        mutableState.value = mutableState.value.copy(running = false, starting = false, url = null, clients = emptyList())
    }

    private fun recordClient(address: String) {
        if (address == "127.0.0.1" || address == "::1") return
        val now = System.currentTimeMillis()
        clients[address] = now
        clients.entries.removeIf { now - it.value > 10 * 60 * 1000 }
        mutableState.value = mutableState.value.copy(
            clients = clients.map { ClientInfo(it.key, it.value) }.sortedByDescending { it.lastSeenAt }
        )
    }
}
