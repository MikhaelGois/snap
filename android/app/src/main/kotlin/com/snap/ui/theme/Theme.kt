package com.snap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFFF6B35),
    secondary = androidx.compose.ui.graphics.Color(0xFFF7931E),
    tertiary = androidx.compose.ui.graphics.Color(0xFF667eea),
    background = androidx.compose.ui.graphics.Color(0xFF0F1419),
    surface = androidx.compose.ui.graphics.Color(0xFF1A1F2B),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onSurface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
)

@Composable
fun SnapTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
