package com.localwatch.server.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DB0FF),
    onPrimary = Color(0xFF10235C),
    primaryContainer = Color(0xFF263C76),
    onPrimaryContainer = Color(0xFFDCE1FF),
    secondary = Color(0xFF65DDBE),
    secondaryContainer = Color(0xFF153E35),
    background = Color(0xFF090D14),
    surface = Color(0xFF090D14),
    surfaceContainer = Color(0xFF121923),
    surfaceVariant = Color(0xFF202938),
    onSurface = Color(0xFFF4F6FB),
    onSurfaceVariant = Color(0xFFA8B3C5),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF3D5DB7),
    primaryContainer = Color(0xFFDCE1FF),
    secondary = Color(0xFF006B57),
    secondaryContainer = Color(0xFF9CF3D8),
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFF7F8FC),
    surfaceContainer = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171B24),
    onSurfaceVariant = Color(0xFF5B6473),
)

@Composable
fun LocalWatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = DarkColors.primary,
    content: @Composable () -> Unit,
) {
    val onAccent = if (accentColor.luminance() > 0.45f) Color(0xFF001F24) else Color.White
    val colors = (if (darkTheme) DarkColors else LightColors).copy(
        primary = accentColor,
        onPrimary = onAccent,
        surfaceTint = accentColor,
    )
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
