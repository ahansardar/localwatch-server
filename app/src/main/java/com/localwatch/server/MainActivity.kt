package com.localwatch.server

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GroupOff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.localwatch.server.data.AppSettings
import com.localwatch.server.model.VideoFile
import com.localwatch.server.server.LocalWatchService
import com.localwatch.server.server.ServerController
import com.localwatch.server.ui.LocalWatchTheme
import com.localwatch.server.updater.UpdateManager
import com.localwatch.server.updater.UpdateOverlay
import com.localwatch.server.updater.UpdateState
import kotlinx.coroutines.launch
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServerController.initialize(this)
        UpdateManager.initialize(this)
        val initialSettings = ServerController.currentSettings()
        OfflinePrimary = Color(initialSettings.accentColor)
        setContent {
            var settings by remember { mutableStateOf(initialSettings) }
            val updateState by UpdateManager.state.collectAsState()
            LocalWatchTheme(
                darkTheme = true,
                accentColor = Color(settings.accentColor)
            ) {
                LocalWatchApp(
                    settings = settings,
                    onSettingsChanged = {
                        OfflinePrimary = Color(it.accentColor)
                        settings = it
                        ServerController.updateSettings(it)
                    }
                )
                UpdateOverlay(updateState, this@MainActivity)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        UpdateManager.onActivityResumed(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalWatchApp(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as Activity
    val state by ServerController.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmStop by remember { mutableStateOf(false) }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val name = runCatching {
                DocumentsContract.getTreeDocumentId(uri).substringAfterLast(':').ifBlank { "Selected folder" }
            }.getOrDefault("Selected folder")
            onSettingsChanged(settings.copy(folderUri = uri, folderName = name))
            ServerController.refreshLibrary()
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (
            BuildConfig.GITHUB_REPO.isNotBlank() &&
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(state.running, settings.keepAwake) {
        if (state.running && settings.keepAwake) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    if (confirmStop) {
        AlertDialog(
            onDismissRequest = { confirmStop = false },
            title = { Text("Stop sharing?") },
            text = { Text("Connected viewers will immediately lose access to the library and any video currently playing.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmStop = false
                    context.stopService(Intent(context, LocalWatchService::class.java))
                    ServerController.stop()
                }) { Text("Stop server", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmStop = false }) { Text("Keep running") } }
        )
    }

    if (!state.running) {
        OfflineDashboard(
            settings = settings,
            starting = state.starting,
            scanning = state.scanning,
            videos = state.videos,
            error = state.error,
            snackbarHost = { SnackbarHost(snackbar) },
            onSettingsChanged = onSettingsChanged,
            onStart = {
                if (Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, LocalWatchService::class.java)
                )
            },
            onChooseFolder = { folderPicker.launch(settings.folderUri) },
            onRescan = ServerController::refreshLibrary
        )
        return
    }

    val runningUrl = state.url.orEmpty()
    RunningDashboard(
        settings = settings,
        url = runningUrl,
        videos = state.videos,
        clients = state.clients.map { it.address },
        scanning = state.scanning,
        error = state.error,
        snackbarHost = { SnackbarHost(snackbar) },
        onSettingsChanged = onSettingsChanged,
        onStop = { confirmStop = true },
        onChooseFolder = { folderPicker.launch(settings.folderUri) },
        onRescan = ServerController::refreshLibrary,
        onCopy = {
            val clipboard = context.getSystemService(ClipboardManager::class.java)
            clipboard.setPrimaryClip(ClipData.newPlainText("LocalWatch link", runningUrl))
            scope.launch { snackbar.showSnackbar("LocalWatch link copied") }
        },
        onShare = {
            context.startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Join my LocalWatch library: $runningUrl")
                    },
                    "Share LocalWatch link"
                )
            )
        }
    )
}

private val OfflineBackground = Color(0xFF0A0A0B)
private val OfflineSurface = Color(0xB8161618)
private val OfflineSurfaceStrong = Color(0xFF201F20)
private val OfflineText = Color(0xFFE5E2E3)
private val OfflineMuted = Color(0xFFBAC9CC)
private val OfflineOutline = Color(0xFF3B494C)
private var OfflinePrimary by mutableStateOf(Color(0xFF9CF0FF))
private val OfflinePurple = Color(0xFF7000FF)
private val OfflineError = Color(0xFFFFB4AB)
private val OfflineWarning = Color(0xFFFFDF96)

private fun offlineOnPrimary(): Color =
    if (OfflinePrimary.luminance() > 0.45f) Color(0xFF001F24) else Color.White

@Composable
private fun BrandLogo(
    modifier: Modifier = Modifier.size(38.dp),
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(R.drawable.localwatch_logo),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

private enum class OfflineTab {
    Dashboard,
    Library,
    Settings,
    Help,
}

@Composable
private fun RunningDashboard(
    settings: AppSettings,
    url: String,
    videos: List<VideoFile>,
    clients: List<String>,
    scanning: Boolean,
    error: String?,
    snackbarHost: @Composable () -> Unit,
    onSettingsChanged: (AppSettings) -> Unit,
    onStop: () -> Unit,
    onChooseFolder: () -> Unit,
    onRescan: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(OfflineTab.Dashboard) }
    Scaffold(
        containerColor = OfflineBackground,
        contentColor = OfflineText,
        snackbarHost = snackbarHost,
        topBar = {
            when (selectedTab) {
                OfflineTab.Settings -> SettingsTopBar()
                OfflineTab.Library -> LibraryTopBar()
                OfflineTab.Help -> HelpTopBar()
                OfflineTab.Dashboard -> RunningTopBar()
            }
        },
        bottomBar = {
            OfflineBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        when (selectedTab) {
            OfflineTab.Settings -> OfflineSettingsContent(
                    settings = settings,
                    onSettingsChanged = onSettingsChanged,
                    modifier = Modifier.padding(padding),
                    serverUrl = url
                )
            OfflineTab.Library -> LibraryContent(
                videos = videos,
                scanning = scanning,
                folderName = settings.folderName,
                onChooseFolder = onChooseFolder,
                onRescan = onRescan,
                modifier = Modifier.padding(padding)
            )
            OfflineTab.Help -> HelpContent(modifier = Modifier.padding(padding))
            OfflineTab.Dashboard -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp,
                    top = 24.dp,
                    end = 20.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    RunningStatusCard(
                        url = url,
                        port = settings.port,
                        onCopy = onCopy,
                        onShare = onShare
                    )
                }
                error?.let { message ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x3393000A)),
                            border = BorderStroke(1.dp, OfflineError.copy(alpha = .3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(message, Modifier.padding(16.dp), color = OfflineError)
                        }
                    }
                }
                item {
                    Button(
                        onClick = onStop,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF93000A),
                            contentColor = Color(0xFFFFDAD6)
                        ),
                        border = BorderStroke(1.dp, OfflineError.copy(alpha = .22f))
                    ) {
                        Icon(Icons.Default.StopCircle, null)
                        Spacer(Modifier.width(9.dp))
                        Text("Stop Server", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                item {
                    RunningQrCard(url = url, onCopy = onCopy, onShare = onShare)
                }
                item {
                    RunningConnectionsCard(clients)
                }
                item {
                    RunningLibraryCard(videos = videos, scanning = scanning)
                }
            }
            }
        }
    }
}

@Composable
private fun RunningTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xB3131314))
            .border(0.5.dp, Color.White.copy(alpha = .08f))
            .statusBarsPadding()
            .height(68.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(Modifier.size(38.dp))
            Spacer(Modifier.width(11.dp))
            Text("LocalWatch", color = OfflinePrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            Modifier.clip(CircleShape)
                .background(OfflinePrimary.copy(alpha = .1f))
                .border(1.dp, OfflinePrimary.copy(alpha = .2f), CircleShape)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(OfflinePrimary))
            Spacer(Modifier.width(7.dp))
            Text(
                "SERVER RUNNING",
                color = OfflinePrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun RunningStatusCard(url: String, port: Int, onCopy: () -> Unit, onShare: () -> Unit) {
    val ip = remember(url) {
        runCatching { Uri.parse(url).host }.getOrNull().orEmpty().ifBlank { "Preparing…" }
    }
    OfflineGlassCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("Server Status: Running", color = OfflinePrimary, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text("Friends on the same Wi-Fi can open this link.", color = OfflineMuted, fontSize = 12.sp)
            }
            IconButton(onClick = onCopy, enabled = url.isNotBlank()) {
                Icon(Icons.Default.ContentCopy, "Copy server link", tint = OfflineMuted)
            }
            IconButton(onClick = onShare, enabled = url.isNotBlank()) {
                Icon(Icons.Default.Share, "Share server link", tint = OfflineMuted)
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp))
                .background(Color(0x800E0E0F))
                .border(1.dp, Color.White.copy(alpha = .05f), RoundedCornerShape(11.dp))
                .padding(15.dp)
        ) {
            RunningDetailRow("LOCAL IP", ip)
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .05f)))
            RunningDetailRow("PORT", port.toString())
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .05f)))
            RunningDetailRow("URL", url.ifBlank { "Generating local address…" })
        }
    }
}

@Composable
private fun RunningDetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = OfflineMuted.copy(alpha = .6f), fontSize = 10.sp, letterSpacing = 1.sp)
        Text(
            value,
            color = OfflinePrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 14.dp)
        )
    }
}

@Composable
private fun RunningQrCard(url: String, onCopy: () -> Unit, onShare: () -> Unit) {
    OfflineGlassCard {
        Text("Scan to Join", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .08f)))
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (url.isBlank()) {
                Box(
                    Modifier.size(192.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = .08f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OfflinePrimary, strokeWidth = 2.dp)
                }
            } else {
                val bitmap = remember(url) { createQrCode(url) }
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR code for $url",
                    modifier = Modifier.size(192.dp).clip(RoundedCornerShape(10.dp))
                        .background(Color.White).padding(10.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            url.ifBlank { "Generating local address…" },
            color = OfflineMuted,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onCopy,
                enabled = url.isNotBlank(),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A2A2B),
                    contentColor = OfflineText
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy", fontSize = 12.sp)
            }
            Button(
                onClick = onShare,
                enabled = url.isNotBlank(),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A2A2B),
                    contentColor = OfflineText
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RunningConnectionsCard(clients: List<String>) {
    OfflineGlassCard {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Connected", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "${clients.size} Active",
                color = OfflinePrimary,
                fontSize = 11.sp,
                modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(OfflinePrimary.copy(alpha = .1f))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .08f)))
        if (clients.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.GroupOff, null, tint = OfflineMuted.copy(alpha = .55f), modifier = Modifier.size(34.dp))
                Spacer(Modifier.height(8.dp))
                Text("Waiting for viewers to join", color = OfflineMuted, fontSize = 12.sp)
            }
        } else {
            clients.forEach { address ->
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(Color(0x1AD1BCFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Smartphone, null, tint = Color(0xFFD1BCFF), modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(address, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(OfflinePrimary))
                            Spacer(Modifier.width(5.dp))
                            Text("Connected", color = OfflineMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RunningLibraryCard(videos: List<VideoFile>, scanning: Boolean) {
    val context = androidx.compose.ui.platform.LocalContext.current
    OfflineGlassCard {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Video Library", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("${videos.size} videos", color = OfflinePrimary, fontSize = 11.sp)
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .08f)))
        when {
            scanning -> {
                Row(Modifier.padding(vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = OfflinePrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Scanning selected folder…", color = OfflineMuted, fontSize = 12.sp)
                }
            }
            videos.isEmpty() -> {
                Text("No supported videos found.", color = OfflineMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 22.dp))
            }
            else -> {
                videos.take(3).forEach { video ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { openVideo(context, video) }
                            .padding(top = 14.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.width(76.dp).height(46.dp).clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFF353436)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = OfflinePrimary)
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(video.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                            Text(formatSize(video.size), color = OfflineMuted, fontSize = 11.sp)
                        }
                    }
                }
                if (videos.size > 3) {
                    Text(
                        "+ ${videos.size - 3} more videos",
                        color = OfflinePrimary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineDashboard(
    settings: AppSettings,
    starting: Boolean,
    scanning: Boolean,
    videos: List<VideoFile>,
    error: String?,
    snackbarHost: @Composable () -> Unit,
    onSettingsChanged: (AppSettings) -> Unit,
    onStart: () -> Unit,
    onChooseFolder: () -> Unit,
    onRescan: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(OfflineTab.Dashboard) }
    Scaffold(
        containerColor = OfflineBackground,
        contentColor = OfflineText,
        snackbarHost = snackbarHost,
        topBar = {
            when (selectedTab) {
                OfflineTab.Settings -> SettingsTopBar()
                OfflineTab.Library -> LibraryTopBar()
                OfflineTab.Help -> HelpTopBar()
                OfflineTab.Dashboard -> OfflineTopBar()
            }
        },
        bottomBar = {
            OfflineBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        when (selectedTab) {
            OfflineTab.Settings -> OfflineSettingsContent(
                settings = settings,
                onSettingsChanged = onSettingsChanged,
                modifier = Modifier.padding(padding)
            )
            OfflineTab.Library -> LibraryContent(
                videos = videos,
                scanning = scanning,
                folderName = settings.folderName,
                onChooseFolder = onChooseFolder,
                onRescan = onRescan,
                modifier = Modifier.padding(padding)
            )
            OfflineTab.Help -> HelpContent(modifier = Modifier.padding(padding))
            OfflineTab.Dashboard -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp,
                    top = 24.dp,
                    end = 20.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
            item {
                OfflineStatusCard(
                    starting = starting,
                    hasFolder = settings.folderUri != null,
                    onStart = onStart
                )
            }
            error?.let { message ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x3393000A)),
                        border = BorderStroke(1.dp, OfflineError.copy(alpha = .3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(message, Modifier.padding(16.dp), color = OfflineError)
                    }
                }
            }
            item {
                OfflineFolderCard(
                    folderName = settings.folderName,
                    scanning = scanning,
                    videos = videos,
                    onChooseFolder = onChooseFolder,
                    onRescan = onRescan
                )
            }
            item { OfflineInstructionsCard() }
            item {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.GroupOff,
                        contentDescription = null,
                        tint = OfflineMuted.copy(alpha = .42f),
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No viewers connected yet",
                        color = OfflineMuted.copy(alpha = .55f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1AFEC931))
                        .border(1.dp, Color(0x33FEC931), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.BatteryChargingFull, null, tint = OfflineWarning)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Streaming can use battery. Keep your phone charged.",
                        color = OfflineWarning,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
            }
        }
    }
}
}

@Composable
private fun OfflineTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xB3131314))
            .border(0.5.dp, Color.White.copy(alpha = .08f))
            .statusBarsPadding()
            .height(68.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BrandLogo(Modifier.size(40.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "LocalWatch Server",
                color = OfflinePrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            )
            Text(
                "Share videos locally without internet",
                color = OfflineMuted.copy(alpha = .7f),
                fontSize = 11.sp
            )
        }
        Row(
            Modifier.clip(CircleShape)
                .background(Color(0x3393000A))
                .border(1.dp, OfflineError.copy(alpha = .2f), CircleShape)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(OfflineError))
            Spacer(Modifier.width(7.dp))
            Text("Server Offline", color = OfflineError, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun OfflineStatusCard(starting: Boolean, hasFolder: Boolean, onStart: () -> Unit) {
    OfflineGlassCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(66.dp).clip(CircleShape).background(Color(0xFF353436)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PowerSettingsNew,
                    null,
                    tint = OfflineMuted,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            Text("Server Status: Stopped", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                if (hasFolder) "Start the server to generate your local watch link."
                else "Choose a video folder to get started.",
                color = OfflineMuted,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onStart,
                enabled = hasFolder && !starting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OfflinePrimary,
                    contentColor = offlineOnPrimary(),
                    disabledContainerColor = OfflinePrimary.copy(alpha = .3f),
                    disabledContentColor = offlineOnPrimary().copy(alpha = .55f)
                )
            ) {
                if (starting) {
                    CircularProgressIndicator(Modifier.size(21.dp), color = offlineOnPrimary(), strokeWidth = 2.dp)
                    Spacer(Modifier.width(9.dp))
                    Text("Starting…", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(7.dp))
                    Text("Start Server", fontWeight = FontWeight.Bold)
                }
            }
            }
    }
}

@Composable
private fun OfflineFolderCard(
    folderName: String,
    scanning: Boolean,
    videos: List<VideoFile>,
    onChooseFolder: () -> Unit,
    onRescan: () -> Unit,
) {
    OfflineGlassCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(11.dp))
                    .background(OfflinePurple.copy(alpha = .18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Folder, null, tint = Color(0xFFD1BCFF))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Video Folder", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (folderName.isBlank()) "No folder selected" else folderName,
                    color = OfflinePrimary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (scanning) "Scanning…" else "${videos.size} videos detected",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(formatSize(videos.sumOf { it.size }), color = OfflineMuted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(17.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onChooseFolder,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, OfflineOutline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OfflineText),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Choose Folder", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = onRescan,
                enabled = folderName.isNotBlank() && !scanning,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, OfflineOutline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OfflineText),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (scanning) "Scanning…" else "Rescan", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun OfflineInstructionsCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = OfflineSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = .08f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {
            Text(
                "How to start a local watch party",
                Modifier.fillMaxWidth().background(Color.White.copy(alpha = .04f)).padding(17.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                listOf(
                    "Turn on hotspot or connect to the same Wi-Fi network.",
                    "Choose the folder containing your media files.",
                    "Tap \"Start Server\" to make your media accessible.",
                    "Have friends scan the QR code that will appear here."
                ).forEachIndexed { index, instruction ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier.size(24.dp).clip(CircleShape).background(OfflinePrimary.copy(alpha = .14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", color = OfflinePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(13.dp))
                        Text(instruction, Modifier.weight(1f), color = OfflineMuted, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineGlassCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OfflineSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = .09f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

private enum class LibrarySort {
    Name,
    Modified,
    FileSize,
}

@Composable
private fun HelpTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xB3131314))
            .border(0.5.dp, Color.White.copy(alpha = .08f))
            .statusBarsPadding()
            .height(68.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(Modifier.size(38.dp))
            Spacer(Modifier.width(11.dp))
            Text("LocalWatch", color = OfflinePrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HelpContent(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedFaq by remember { mutableStateOf<String?>(null) }
    var selectedGuide by remember { mutableStateOf<String?>(null) }
    var hotspotChecked by remember { mutableStateOf(false) }
    var networkChecked by remember { mutableStateOf(false) }
    var restartChecked by remember { mutableStateOf(false) }
    val faqItems = remember {
        listOf(
            Triple(
                "Video Playback",
                Icons.Default.PlayArrow,
                "Use a modern browser with support for your video's format. MP4 with H.264 offers the widest compatibility."
            ),
            Triple(
                "Network & Wi-Fi",
                Icons.Default.Wifi,
                "The server phone and every viewer must be connected to the same Wi-Fi network or phone hotspot."
            ),
            Triple(
                "Security & Privacy",
                Icons.Default.Lock,
                "LocalWatch stays on your local network. Enable a viewer PIN in Settings when sharing on a network you do not fully trust."
            ),
            Triple(
                "Device Compatibility",
                Icons.Default.Smartphone,
                "Any phone, tablet, computer, or smart TV with a current web browser can open the LocalWatch address."
            )
        )
    }
    val filteredFaqs = remember(query) {
        if (query.isBlank()) faqItems
        else faqItems.filter { (title, _, answer) ->
            title.contains(query, ignoreCase = true) || answer.contains(query, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            top = 22.dp,
            end = 20.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null, tint = OfflineMuted) },
                placeholder = { Text("Search FAQ topics", color = OfflineMuted.copy(alpha = .6f)) },
                colors = offlineTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("Quick Start Guides", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                HelpGuideCard(
                    icon = Icons.Default.PlayArrow,
                    title = "Setting up your first party",
                    description = "Choose a media folder, start the server, then share its QR code.",
                    color = OfflinePrimary,
                    onClick = {
                        selectedGuide = if (selectedGuide == "party") null else "party"
                    }
                )
                HelpGuideCard(
                    icon = Icons.Default.Smartphone,
                    title = "Connecting devices",
                    description = "Open the server address from phones, tablets, computers, or TVs.",
                    color = Color(0xFFD1BCFF),
                    onClick = {
                        selectedGuide = if (selectedGuide == "devices") null else "devices"
                    }
                )
                HelpGuideCard(
                    icon = Icons.Default.WifiOff,
                    title = "Connection troubleshooting",
                    description = "Quick checks for devices that cannot find or open the server.",
                    color = OfflineError,
                    onClick = {
                        selectedGuide = if (selectedGuide == "connection") null else "connection"
                    }
                )
            }
            selectedGuide?.let { guide ->
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = OfflineSurface),
                    border = BorderStroke(1.dp, OfflinePrimary.copy(alpha = .2f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        when (guide) {
                            "party" -> "1. Choose your video folder.\n2. Connect everyone to the same Wi-Fi or hotspot.\n3. Start the server.\n4. Share the QR code or local address."
                            "devices" -> "1. Keep the server screen open.\n2. Scan the QR code from another device.\n3. Open the link in a current browser.\n4. Enter the viewer PIN if enabled."
                            else -> "1. Confirm both devices use the same network.\n2. Disable client isolation if the router provides it.\n3. Verify the displayed IP address.\n4. Stop and restart the server."
                        },
                        color = OfflineMuted,
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        item {
            Text("FAQ Categories", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            if (filteredFaqs.isEmpty()) {
                OfflineGlassCard {
                    Text("No help topics match \"$query\".", color = OfflineMuted, fontSize = 13.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredFaqs.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { (title, icon, _) ->
                                HelpCategoryCard(
                                    title = title,
                                    icon = icon,
                                    selected = selectedFaq == title,
                                    onClick = {
                                        selectedFaq = if (selectedFaq == title) null else title
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                selectedFaq?.let { selected ->
                    filteredFaqs.firstOrNull { it.first == selected }?.let { topic ->
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OfflinePrimary.copy(alpha = .08f)),
                            border = BorderStroke(1.dp, OfflinePrimary.copy(alpha = .18f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(topic.first, color = OfflinePrimary, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text(topic.third, color = OfflineMuted, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }
        item {
            OfflineGlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Checklist, null, tint = OfflinePrimary)
                    Spacer(Modifier.width(9.dp))
                    Text("Troubleshooting Checklist", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(15.dp))
                HelpChecklistRow("Check if hotspot is on", hotspotChecked) { hotspotChecked = it }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .05f)))
                HelpChecklistRow("Ensure devices use the same Wi-Fi", networkChecked) { networkChecked = it }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .05f)))
                HelpChecklistRow("Restart the LocalWatch server", restartChecked) { restartChecked = it }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OfflineSurface),
                border = BorderStroke(1.dp, OfflinePrimary.copy(alpha = .2f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Still need help?", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Send app details and a description of the problem through your preferred feedback app.",
                        color = OfflineMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "LocalWatch feedback")
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "LocalWatch ${BuildConfig.VERSION_NAME} feedback\n\nWhat happened:\n\nSteps to reproduce:\n"
                                        )
                                    },
                                    "Send LocalWatch feedback"
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OfflinePrimary,
                            contentColor = offlineOnPrimary()
                        ),
                        shape = RoundedCornerShape(13.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Default.Feedback, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Send Feedback", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpGuideCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.width(272.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = OfflineSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = .09f)),
        shape = RoundedCornerShape(17.dp)
    ) {
        Column(Modifier.padding(19.dp)) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(11.dp)).background(color.copy(alpha = .13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            Spacer(Modifier.height(14.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            Text(description, color = OfflineMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun HelpCategoryCard(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.clip(RoundedCornerShape(15.dp))
            .background(if (selected) OfflinePrimary.copy(alpha = .1f) else OfflineSurface)
            .border(
                1.dp,
                if (selected) OfflinePrimary.copy(alpha = .3f) else Color.White.copy(alpha = .08f),
                RoundedCornerShape(15.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Icon(icon, null, tint = OfflinePrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HelpChecklistRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChecked(!checked) }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChecked,
            colors = CheckboxDefaults.colors(
                checkedColor = OfflinePrimary,
                checkmarkColor = offlineOnPrimary(),
                uncheckedColor = OfflineOutline
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            color = OfflineMuted.copy(alpha = if (checked) .45f else 1f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun LibraryTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xB3131314))
            .border(0.5.dp, Color.White.copy(alpha = .08f))
            .statusBarsPadding()
            .height(68.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(Modifier.size(38.dp))
            Spacer(Modifier.width(11.dp))
            Text("LocalWatch", color = OfflinePrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LibraryContent(
    videos: List<VideoFile>,
    scanning: Boolean,
    folderName: String,
    onChooseFolder: () -> Unit,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(LibrarySort.Name) }
    val visibleVideos = remember(videos, query, sort) {
        val filtered = videos.filter { it.name.contains(query.trim(), ignoreCase = true) }
        when (sort) {
            LibrarySort.Name -> filtered.sortedBy { it.name.lowercase(Locale.getDefault()) }
            LibrarySort.Modified -> filtered.sortedByDescending { it.modifiedAt }
            LibrarySort.FileSize -> filtered.sortedByDescending { it.size }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            top = 24.dp,
            end = 20.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "MEDIA ECOSYSTEM",
                color = OfflinePrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.6.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Video Library", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (folderName.isBlank()) "No media folder selected"
                        else "${videos.size} videos • ${formatSize(videos.sumOf { it.size })}",
                        color = OfflineMuted,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onRescan, enabled = folderName.isNotBlank() && !scanning) {
                    if (scanning) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = OfflinePrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, "Rescan library", tint = OfflinePrimary)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LibrarySortChip("Name", sort == LibrarySort.Name) { sort = LibrarySort.Name }
                LibrarySortChip("Modified", sort == LibrarySort.Modified) { sort = LibrarySort.Modified }
                LibrarySortChip("File Size", sort == LibrarySort.FileSize) { sort = LibrarySort.FileSize }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null, tint = OfflineMuted) },
                placeholder = { Text("Search your local media library…", color = OfflineMuted.copy(alpha = .65f)) },
                colors = offlineTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (visibleVideos.isEmpty()) {
            item {
                Column(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 24.dp, vertical = 44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (query.isBlank()) Icons.Default.VideoLibrary else Icons.Default.Search,
                        null,
                        tint = OfflineMuted,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(Modifier.height(13.dp))
                    Text(
                        if (query.isBlank()) "Your video library is empty" else "No matching videos found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        if (query.isBlank()) "Choose a folder containing videos to synchronize with LocalWatch."
                        else "Try another title or clear the search.",
                        color = OfflineMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (query.isBlank()) {
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = onChooseFolder,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OfflinePrimary,
                                contentColor = offlineOnPrimary()
                            ),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Folder, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(if (folderName.isBlank()) "Add Media Source" else "Change Media Source")
                        }
                    }
                }
            }
        } else {
            items(visibleVideos, key = { it.id }) { video ->
                VideoLibraryItem(video, onOpen = { openVideo(context, video) })
            }
            item {
                OutlinedButton(
                    onClick = onChooseFolder,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, OfflineOutline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OfflinePrimary)
                ) {
                    Icon(Icons.Default.Folder, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Change Media Source")
                }
            }
        }
    }
}

@Composable
private fun LibrarySortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) OfflinePrimary else OfflineSurface,
            contentColor = if (selected) offlineOnPrimary() else OfflineMuted
        ),
        border = if (selected) null else BorderStroke(1.dp, Color.White.copy(alpha = .08f)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 13.dp, vertical = 7.dp),
        modifier = Modifier.height(34.dp)
    ) {
        Text(label, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun VideoLibraryItem(video: VideoFile, onOpen: () -> Unit) {
    val extension = remember(video.name) {
        video.name.substringAfterLast('.', "").uppercase(Locale.getDefault()).ifBlank { "VIDEO" }
    }
    val modified = remember(video.modifiedAt) {
        if (video.modifiedAt <= 0L) "Date unavailable"
        else SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(video.modifiedAt))
    }
    val thumbnailColors = remember(video.id) {
        val palettes = listOf(
            listOf(Color(0xFF083344), Color(0xFF4C1D95)),
            listOf(Color(0xFF052E16), Color(0xFF365314)),
            listOf(Color(0xFF431407), Color(0xFF7F1D1D)),
            listOf(Color(0xFF172554), Color(0xFF312E81))
        )
        palettes[(video.id.hashCode() and Int.MAX_VALUE) % palettes.size]
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = OfflineSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = .08f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                .background(Brush.linearGradient(thumbnailColors)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                null,
                tint = Color.White.copy(alpha = .9f),
                modifier = Modifier.size(48.dp)
            )
            Row(
                Modifier.align(Alignment.BottomStart).padding(13.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    extension,
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.clip(RoundedCornerShape(5.dp))
                        .background(Color.Black.copy(alpha = .5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Column(Modifier.padding(16.dp)) {
            Text(video.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(formatSize(video.size), color = OfflineMuted, fontSize = 12.sp)
                Text(modified, color = OfflineMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(13.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    extension,
                    color = Color(0xFFD1BCFF),
                    fontSize = 10.sp,
                    modifier = Modifier.clip(CircleShape)
                        .background(OfflinePurple.copy(alpha = .18f))
                        .border(1.dp, OfflinePurple.copy(alpha = .28f), CircleShape)
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(OfflinePrimary))
                    Spacer(Modifier.width(6.dp))
                    Text("Ready", color = OfflinePrimary, fontSize = 11.sp)
                }
            }
        }
    }
}

private fun openVideo(context: Context, video: VideoFile) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(video.uri, video.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        )
    }.onFailure {
        android.widget.Toast.makeText(
            context,
            "No installed player can open ${video.name}.",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
private fun SettingsTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xB3131314))
            .border(0.5.dp, Color.White.copy(alpha = .08f))
            .statusBarsPadding()
            .height(68.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(Modifier.size(38.dp))
            Spacer(Modifier.width(11.dp))
            Text("LocalWatch", color = OfflinePrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OfflineSettingsContent(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    modifier: Modifier = Modifier,
    serverUrl: String? = null,
) {
    var portText by remember(settings.port) { mutableStateOf(settings.port.toString()) }
    var showPin by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val updateState by UpdateManager.state.collectAsState()
    if (showAbout) {
        AboutContent(
            onBack = { showAbout = false },
            modifier = modifier
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            top = 24.dp,
            end = 20.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Settings", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("v${BuildConfig.VERSION_NAME}", color = OfflineMuted, fontSize = 10.sp, letterSpacing = 1.sp)
            }
        }
        item {
            SettingsSection(Icons.Default.Tune, "GENERAL") {
                SettingsToggleRow(
                    title = "Allow downloads",
                    description = "Enable offline playback on viewer devices",
                    checked = settings.allowDownloads,
                    onChecked = { onSettingsChanged(settings.copy(allowDownloads = it)) }
                )
                SettingsToggleRow(
                    title = "Keep screen awake",
                    description = "Prevent dimming during active streaming",
                    checked = settings.keepAwake,
                    onChecked = { onSettingsChanged(settings.copy(keepAwake = it)) }
                )
            }
        }
        item {
            SettingsSection(Icons.Default.Security, "SECURITY") {
                SettingsToggleRow(
                    title = "Require PIN",
                    description = "Protect server access with a numeric code",
                    checked = settings.requirePin,
                    onChecked = { onSettingsChanged(settings.copy(requirePin = it)) }
                )
                if (settings.requirePin) {
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .06f)))
                    Spacer(Modifier.height(13.dp))
                    Text("Current PIN", color = OfflineMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                    Spacer(Modifier.height(7.dp))
                    OutlinedTextField(
                        value = settings.pin,
                        onValueChange = {
                            onSettingsChanged(settings.copy(pin = it.filter(Char::isDigit).take(12)))
                        },
                        visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showPin = !showPin }) {
                                Icon(
                                    if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    if (showPin) "Hide PIN" else "Show PIN",
                                    tint = OfflineMuted
                                )
                            }
                        },
                        colors = offlineTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            SettingsSection(Icons.Default.Wifi, "NETWORK") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Port Configuration", color = OfflineMuted, fontSize = 12.sp)
                    Text("DEFAULT: 8080", color = OfflinePrimary.copy(alpha = .65f), fontSize = 10.sp)
                }
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = portText,
                    onValueChange = { value ->
                        portText = value.filter(Char::isDigit).take(5)
                        portText.toIntOrNull()
                            ?.takeIf { it in 1024..65535 }
                            ?.let { onSettingsChanged(settings.copy(port = it)) }
                    },
                    singleLine = true,
                    enabled = serverUrl == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Icon(Icons.Default.Info, "Valid ports: 1024 to 65535", tint = OfflineMuted) },
                    colors = offlineTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp))
                        .background(OfflinePurple.copy(alpha = .1f))
                        .border(1.dp, OfflinePurple.copy(alpha = .22f), RoundedCornerShape(11.dp))
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Wifi, null, tint = Color(0xFFD1BCFF), modifier = Modifier.size(21.dp))
                    Spacer(Modifier.width(11.dp))
                    Text(
                        if (serverUrl.isNullOrBlank()) {
                            "Server address will be generated when sharing starts on port ${settings.port}."
                        } else {
                            "Remote Access Active: Your server is currently reachable at $serverUrl"
                        },
                        color = OfflineMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
        item {
            SettingsSection(Icons.Default.Palette, "APPEARANCE") {
                Text("Accent color", color = OfflineMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(
                        "Electric cyan" to 0xFF9CF0FF.toInt(),
                        "Neon purple" to 0xFF7000FF.toInt(),
                        "Cyber pink" to 0xFFFF4F8E.toInt(),
                        "Matrix green" to 0xFF00FF9C.toInt(),
                        "Amber" to 0xFFFFD700.toInt()
                    ).forEach { (name, accent) ->
                        val selected = settings.accentColor == accent
                        Box(
                            Modifier.size(42.dp)
                                .then(
                                    if (selected) Modifier.border(2.dp, Color(accent), CircleShape).padding(4.dp)
                                    else Modifier
                                )
                                .clip(CircleShape)
                                .background(Color(accent))
                                .semantics {
                                    contentDescription = "$name accent"
                                    this.selected = selected
                                    role = Role.Button
                                }
                                .clickable { onSettingsChanged(settings.copy(accentColor = accent)) }
                        )
                    }
                }
            }
        }
        item {
            SettingsSection(Icons.Default.SystemUpdate, "UPDATES") {
                Button(
                    onClick = { UpdateManager.checkNow() },
                    enabled = updateState !is UpdateState.Checking &&
                        updateState !is UpdateState.Downloading &&
                        updateState !is UpdateState.Installing,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OfflinePrimary,
                        contentColor = offlineOnPrimary()
                    )
                ) {
                    if (updateState is UpdateState.Checking) {
                        CircularProgressIndicator(
                            Modifier.size(19.dp),
                            color = offlineOnPrimary(),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.SystemUpdate, null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (updateState is UpdateState.Checking) "Checking…" else "Check for Updates",
                        fontWeight = FontWeight.Bold
                    )
                }
                val statusMessage = when (val current = updateState) {
                    UpdateState.UpToDate -> "LocalWatch is up to date."
                    is UpdateState.Available -> "${current.release.tag} is ready to download."
                    is UpdateState.Downloading -> "Downloading ${current.release.tag}…"
                    is UpdateState.Installing -> "Installing ${current.release.tag}…"
                    is UpdateState.NeedsInstallPermission -> "Android install permission is required."
                    is UpdateState.Error -> current.message
                    UpdateState.Checking -> "Checking the configured GitHub release feed…"
                    UpdateState.Idle -> null
                }
                statusMessage?.let { message ->
                    Spacer(Modifier.height(10.dp))
                    Text(message, color = OfflineMuted, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }
        item {
            SettingsSection(Icons.Default.Info, "ABOUT") {
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .clickable { showAbout = true }
                        .padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                            .background(OfflinePrimary.copy(alpha = .12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        BrandLogo(Modifier.size(34.dp))
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("About LocalWatch", fontWeight = FontWeight.Medium)
                        Text("Version, project details, credits, and legal", color = OfflineMuted, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = OfflineMuted)
                }
            }
        }
    }
}

@Composable
private fun AboutContent(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var expandedLegal by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            top = 18.dp,
            end = 20.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back to settings", tint = OfflinePrimary)
                }
                Spacer(Modifier.width(4.dp))
                Text("About", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(94.dp).clip(RoundedCornerShape(26.dp))
                        .background(OfflineSurface)
                        .border(1.dp, Color.White.copy(alpha = .12f), RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BrandLogo(Modifier.size(72.dp), "LocalWatch logo")
                }
                Spacer(Modifier.height(18.dp))
                Text("LocalWatch", color = OfflinePrimary, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                Text("v${BuildConfig.VERSION_NAME}", color = OfflineMuted, fontSize = 10.sp, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(18.dp))
                Text(
                    "Share your media library with friends and family completely offline—no accounts, subscriptions, or internet connection required.",
                    color = OfflineMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AboutFactCard(Icons.Default.Code, "Kotlin", Modifier.weight(1f))
                AboutFactCard(Icons.Default.Smartphone, "Android 8+", Modifier.weight(1f))
                AboutFactCard(Icons.Default.Policy, "MIT", Modifier.weight(1f))
            }
        }
        item {
            Text(
                "CREDITS & TEAM",
                color = OfflineMuted,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )
            OfflineGlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(OfflinePurple.copy(alpha = .22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, null, tint = Color(0xFFD1BCFF))
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Ahan Sardar", fontWeight = FontWeight.SemiBold)
                        Text("Creator and maintainer", color = OfflineMuted, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/ahansardar/localwatch-server")
                                )
                            )
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Code, null, tint = OfflinePrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("View project on GitHub", fontWeight = FontWeight.Medium)
                        Text("Source, releases, and issue tracker", color = OfflineMuted, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = OfflineMuted)
                }
            }
        }
        item {
            Text(
                "LEGAL",
                color = OfflineMuted,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = OfflineSurface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = .09f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column {
                    AboutLegalRow("Privacy Notice") {
                        expandedLegal = if (expandedLegal == "privacy") null else "privacy"
                    }
                    if (expandedLegal == "privacy") {
                        AboutLegalText(
                            "LocalWatch does not collect, upload, sell, or share personal data. Media remains on the host device and recent client addresses exist only in memory."
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .05f)))
                    AboutLegalRow("Open Source License") {
                        expandedLegal = if (expandedLegal == "license") null else "license"
                    }
                    if (expandedLegal == "license") {
                        AboutLegalText(
                            "LocalWatch is provided under the MIT License. Copyright © 2026 Ahan Sardar."
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .05f)))
                    AboutLegalRow("Network & Privacy Details") {
                        expandedLegal = if (expandedLegal == "network") null else "network"
                    }
                    if (expandedLegal == "network") {
                        AboutLegalText(
                            "Streaming uses unencrypted HTTP on the local network. Enable PIN protection on shared networks and share only media you have the right to distribute."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutFactCard(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(15.dp))
            .background(OfflineSurface)
            .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(15.dp))
            .padding(vertical = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = OfflinePrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AboutLegalRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Icon(Icons.Default.ChevronRight, null, tint = OfflineMuted)
    }
}

@Composable
private fun AboutLegalText(text: String) {
    Text(
        text,
        color = OfflineMuted,
        fontSize = 11.sp,
        lineHeight = 17.sp,
        modifier = Modifier.fillMaxWidth()
            .background(Color.White.copy(alpha = .025f))
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    )
}

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Row(
            Modifier.padding(start = 4.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = OfflineMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, color = OfflineMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp)
        }
        OfflineGlassCard(content)
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = OfflineText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(description, color = OfflineMuted, fontSize = 11.sp, lineHeight = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OfflinePrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = OfflineOutline.copy(alpha = .45f),
                disabledCheckedThumbColor = Color.White.copy(alpha = .7f),
                disabledCheckedTrackColor = OfflinePrimary.copy(alpha = .45f)
            )
        )
    }
}

@Composable
private fun offlineTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = OfflineText,
    unfocusedTextColor = OfflineText,
    focusedBorderColor = OfflinePrimary,
    unfocusedBorderColor = OfflineOutline.copy(alpha = .45f),
    cursorColor = OfflinePrimary,
    focusedContainerColor = Color(0x800E0E0F),
    unfocusedContainerColor = Color(0x800E0E0F)
)

@Composable
private fun OfflineBottomNavigation(
    selectedTab: OfflineTab,
    onTabSelected: (OfflineTab) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xE61C1B1C))
            .border(0.5.dp, Color.White.copy(alpha = .08f))
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OfflineNavItem(
            Icons.Default.Dashboard,
            "Dashboard",
            selected = selectedTab == OfflineTab.Dashboard,
            onClick = { onTabSelected(OfflineTab.Dashboard) }
        )
        OfflineNavItem(
            Icons.Default.VideoLibrary,
            "Library",
            selected = selectedTab == OfflineTab.Library,
            onClick = { onTabSelected(OfflineTab.Library) }
        )
        OfflineNavItem(
            Icons.Default.Settings,
            "Settings",
            selected = selectedTab == OfflineTab.Settings,
            onClick = { onTabSelected(OfflineTab.Settings) }
        )
        OfflineNavItem(
            Icons.Default.HelpOutline,
            "Help",
            selected = selectedTab == OfflineTab.Help,
            onClick = { onTabSelected(OfflineTab.Help) }
        )
    }
}

@Composable
private fun OfflineNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Column(
        Modifier.clip(RoundedCornerShape(22.dp))
            .background(if (selected) OfflinePurple else Color.Transparent)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 15.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = if (selected) Color(0xFFDDCDFF) else OfflineMuted, modifier = Modifier.size(22.dp))
        Text(label, color = if (selected) Color(0xFFDDCDFF) else OfflineMuted, fontSize = 10.sp)
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(17.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text("LocalWatch Server", fontWeight = FontWeight.ExtraBold, fontSize = 23.sp)
            Text("Share videos locally without internet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ServerStatusCard(
    running: Boolean,
    starting: Boolean,
    url: String?,
    port: Int,
    hasFolder: Boolean,
    onToggle: () -> Unit,
) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(11.dp).clip(CircleShape)
                    .background(if (running) Color(0xFF51D8A7) else Color(0xFF778195))
            )
            Spacer(Modifier.width(9.dp))
            Text(if (running) "SERVER RUNNING" else "SERVER STOPPED", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.2.sp)
        }
        Spacer(Modifier.height(18.dp))
        Text(
            url ?: if (hasFolder) "Ready on port $port" else "Choose a folder to get started",
            fontSize = if (url != null) 21.sp else 17.sp,
            fontWeight = FontWeight.Bold
        )
        if (running) {
            Text("Nearby devices can open this address", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onToggle,
            enabled = !starting && (running || hasFolder),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (running) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            if (starting) {
                CircularProgressIndicator(Modifier.size(21.dp), strokeWidth = 2.dp)
            } else {
                Icon(if (running) Icons.Default.PowerSettingsNew else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text(if (running) "Stop server" else "Start server", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FolderCard(folderName: String, scanning: Boolean, videoCount: Int, onChoose: () -> Unit, onRefresh: () -> Unit) {
    PremiumCard {
        SectionTitle(Icons.Default.Folder, "Video folder")
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (folderName.isBlank()) "No folder selected" else folderName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (scanning) "Scanning for videos…" else if (folderName.isBlank()) "Select a folder using Android's secure picker" else "$videoCount videos detected",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            if (folderName.isNotBlank()) {
                IconButton(onClick = onRefresh, enabled = !scanning) { Icon(Icons.Default.Refresh, "Rescan") }
            }
        }
        Spacer(Modifier.height(13.dp))
        OutlinedButton(onClick = onChoose, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Folder, null)
            Spacer(Modifier.width(8.dp))
            Text(if (folderName.isBlank()) "Choose video folder" else "Change folder")
        }
    }
}

@Composable
private fun ShareCard(url: String, onCopy: () -> Unit, onShare: () -> Unit) {
    PremiumCard {
        SectionTitle(Icons.Default.Link, "Invite viewers")
        Spacer(Modifier.height(16.dp))
        val bitmap = remember(url) { createQrCode(url) }
        Box(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR code for $url",
                modifier = Modifier.size(218.dp).clip(RoundedCornerShape(18.dp)).background(Color.White).padding(12.dp)
            )
        }
        Text(url, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(13.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCopy, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ContentCopy, null)
                Spacer(Modifier.width(6.dp))
                Text("Copy")
            }
            Button(onClick = onShare, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Share, null)
                Spacer(Modifier.width(6.dp))
                Text("Share")
            }
        }
    }
}

@Composable
private fun ConnectionsCard(clients: List<String>, running: Boolean) {
    PremiumCard {
        SectionTitle(Icons.Default.People, "Connected devices")
        Spacer(Modifier.height(13.dp))
        Text(
            if (!running) "Start the server to see viewers"
            else if (clients.isEmpty()) "Waiting for someone to join…"
            else "${clients.size} active ${if (clients.size == 1) "device" else "devices"}",
            fontWeight = FontWeight.SemiBold
        )
        clients.take(4).forEach {
            Row(Modifier.padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF51D8A7), modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun LibraryPreview(videos: List<VideoFile>, scanning: Boolean) {
    PremiumCard {
        SectionTitle(Icons.Default.Movie, "Library preview")
        Spacer(Modifier.height(12.dp))
        if (scanning) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Scanning selected folder…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (videos.isEmpty()) {
            Text("No supported videos found yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            videos.take(5).forEachIndexed { index, video ->
                if (index > 0) Spacer(Modifier.height(11.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(video.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                        Text(formatSize(video.size), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
            if (videos.size > 5) {
                Text("+ ${videos.size - 5} more videos", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, modifier = Modifier.padding(top = 13.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(settings: AppSettings, serverRunning: Boolean, onChange: (AppSettings) -> Unit) {
    var portText by remember(settings.port) { mutableStateOf(settings.port.toString()) }
    PremiumCard {
        SectionTitle(Icons.Default.Settings, "Server settings")
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = portText,
            onValueChange = {
                portText = it.filter(Char::isDigit).take(5)
                it.toIntOrNull()?.takeIf { port -> port in 1024..65535 }?.let { port ->
                    onChange(settings.copy(port = port))
                }
            },
            enabled = !serverRunning,
            label = { Text("Port") },
            supportingText = { Text(if (serverRunning) "Stop server to change port" else "Use 1024–65535") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        SettingSwitch("Allow video downloads", settings.allowDownloads) { onChange(settings.copy(allowDownloads = it)) }
        SettingSwitch("Require viewer PIN", settings.requirePin) { onChange(settings.copy(requirePin = it)) }
        AnimatedVisibility(settings.requirePin) {
            OutlinedTextField(
                value = settings.pin,
                onValueChange = { value -> onChange(settings.copy(pin = value.filter(Char::isDigit).take(12))) },
                label = { Text("Viewer PIN") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )
        }
        SettingSwitch("Keep screen awake while sharing", settings.keepAwake) { onChange(settings.copy(keepAwake = it)) }
        SettingSwitch("Dark mode", settings.darkMode) { onChange(settings.copy(darkMode = it)) }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun InstructionsCard() {
    PremiumCard {
        SectionTitle(Icons.Default.Wifi, "How to host")
        Spacer(Modifier.height(12.dp))
        listOf(
            "Turn on your hotspot or connect everyone to the same Wi-Fi.",
            "Choose a folder, then start the LocalWatch server.",
            "Ask friends to scan the QR code or open the link.",
            "Pick a video in the browser and press play."
        ).forEachIndexed { index, text ->
            Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(25.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) { Text("${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                Spacer(Modifier.width(10.dp))
                Text(text, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Text(message, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Composable
private fun InfoBanner(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .55f))
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun PremiumCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(19.dp), content = content)
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(9.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

private fun createQrCode(content: String): Bitmap {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 640, 640)
    return Bitmap.createBitmap(640, 640, Bitmap.Config.ARGB_8888).apply {
        for (x in 0 until 640) for (y in 0 until 640) {
            setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val group = (kotlin.math.ln(bytes.toDouble()) / kotlin.math.ln(1024.0)).toInt().coerceIn(0, units.lastIndex)
    return String.format(Locale.US, if (group == 0) "%.0f %s" else "%.1f %s", bytes / Math.pow(1024.0, group.toDouble()), units[group])
}
