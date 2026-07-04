package com.localwatch.server.updater

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun UpdateOverlay(state: UpdateState, context: Context) {
    when (state) {
        is UpdateState.Available -> AlertDialog(
            onDismissRequest = UpdateManager::dismiss,
            title = { Text("${state.release.title} is available") },
            text = {
                Column {
                    Text(
                        "Installed: ${com.localwatch.server.BuildConfig.VERSION_NAME}  •  New: ${state.release.tag}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    MarkdownReleaseNotes(state.release.markdownBody)
                    if (state.release.apkSize > 0) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Download size: ${formatBytes(state.release.apkSize)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { UpdateManager.downloadAndInstall(state.release) }) {
                    Text("Download & install")
                }
            },
            dismissButton = {
                TextButton(onClick = UpdateManager::dismiss) { Text("Later") }
            }
        )

        is UpdateState.Downloading -> {
            val determinate = state.total > 0
            val progress = if (determinate) {
                (state.downloaded.toFloat() / state.total.toFloat()).coerceIn(0f, 1f)
            } else 0f
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Downloading ${state.release.tag}") },
                text = {
                    Column {
                        if (determinate) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "${(progress * 100).roundToInt()}%  •  ${formatBytes(state.downloaded)} / ${formatBytes(state.total)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "${formatBytes(state.downloaded)} downloaded",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("The APK will be verified before installation.")
                    }
                },
                confirmButton = {}
            )
        }

        is UpdateState.NeedsInstallPermission -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Allow LocalWatch updates") },
            text = {
                Text(
                    "Android requires one-time permission for LocalWatch to install APK updates. Enable “Allow from this source,” then return to the app."
                )
            },
            confirmButton = {
                Button(onClick = { UpdateManager.openInstallPermission(context) }) {
                    Text("Open Android settings")
                }
            },
            dismissButton = {
                TextButton(onClick = UpdateManager::dismiss) { Text("Cancel") }
            }
        )

        is UpdateState.Installing -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Installing ${state.release.tag}") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(modifier = Modifier.width(24.dp))
                    Text("Android is staging the verified APK. The app may restart automatically.")
                }
            },
            confirmButton = {}
        )

        is UpdateState.Error -> AlertDialog(
            onDismissRequest = UpdateManager::dismiss,
            title = { Text("Update failed") },
            text = { Text(state.message) },
            confirmButton = {
                if (state.release != null) {
                    Button(onClick = { UpdateManager.downloadAndInstall(state.release) }) {
                        Text("Retry download")
                    }
                } else {
                    Button(onClick = { UpdateManager.checkNow() }) { Text("Try again") }
                }
            },
            dismissButton = {
                TextButton(onClick = UpdateManager::dismiss) { Text("Close") }
            }
        )

        UpdateState.Checking,
        UpdateState.Idle,
        UpdateState.UpToDate -> Unit
    }
}

@Composable
private fun MarkdownReleaseNotes(markdown: String) {
    val lines = markdown.ifBlank { "No release notes were provided." }.lines()
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        items(lines) { source ->
            val line = source.trimEnd()
            when {
                line.isBlank() -> Spacer(Modifier.height(3.dp))
                line.startsWith("### ") -> MarkdownInline(line.removePrefix("### "), 15, FontWeight.Bold)
                line.startsWith("## ") -> MarkdownInline(line.removePrefix("## "), 17, FontWeight.Bold)
                line.startsWith("# ") -> MarkdownInline(line.removePrefix("# "), 19, FontWeight.Bold)
                line.matches(Regex("^[-*+]\\s+.*")) -> {
                    Row {
                        Text("•", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            MarkdownInline(line.replaceFirst(Regex("^[-*+]\\s+"), ""), 13, FontWeight.Normal)
                        }
                    }
                }
                line.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val marker = line.substringBefore(' ') + " "
                    Row {
                        Text(marker, color = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f)) {
                            MarkdownInline(line.removePrefix(marker), 13, FontWeight.Normal)
                        }
                    }
                }
                line.startsWith("> ") -> Text(
                    line.removePrefix("> "),
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
                            RoundedCornerShape(7.dp)
                        )
                        .padding(9.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                line.startsWith("```") -> Unit
                line.startsWith("    ") -> Text(
                    line.trimStart(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
                            RoundedCornerShape(7.dp)
                        )
                        .padding(9.dp)
                )
                else -> MarkdownInline(line, 13, FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun MarkdownInline(source: String, fontSize: Int, weight: FontWeight) {
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.primary
    val text = buildAnnotatedString {
        var index = 0
        while (index < source.length) {
            when {
                source.startsWith("**", index) -> {
                    val end = source.indexOf("**", index + 2)
                    if (end > index) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(source.substring(index + 2, end))
                        }
                        index = end + 2
                    } else {
                        append(source[index++])
                    }
                }
                source[index] == '`' -> {
                    val end = source.indexOf('`', index + 1)
                    if (end > index) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color.White.copy(alpha = .08f)
                            )
                        ) {
                            append(source.substring(index + 1, end))
                        }
                        index = end + 1
                    } else {
                        append(source[index++])
                    }
                }
                source[index] == '[' -> {
                    val labelEnd = source.indexOf(']', index + 1)
                    val urlStart = if (labelEnd >= 0) source.indexOf('(', labelEnd) else -1
                    val urlEnd = if (urlStart >= 0) source.indexOf(')', urlStart) else -1
                    if (labelEnd > index && urlStart == labelEnd + 1 && urlEnd > urlStart) {
                        withStyle(
                            SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            val label = source.substring(index + 1, labelEnd)
                            pushStringAnnotation("URL", source.substring(urlStart + 1, urlEnd))
                            append(label)
                            pop()
                        }
                        index = urlEnd + 1
                    } else {
                        append(source[index++])
                    }
                }
                else -> append(source[index++])
            }
        }
    }
    @Suppress("DEPRECATION")
    ClickableText(
        text = text,
        style = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = fontSize.sp,
            fontWeight = weight,
            lineHeight = (fontSize + 6).sp
        ),
        onClick = { offset ->
            text.getStringAnnotations("URL", offset, offset)
                .firstOrNull()
                ?.let { runCatching { uriHandler.openUri(it.item) } }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "Unknown size"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return if (unit == 0) "${value.toLong()} ${units[unit]}"
    else String.format(java.util.Locale.US, "%.1f %s", value, units[unit])
}
