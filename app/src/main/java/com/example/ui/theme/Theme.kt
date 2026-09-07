package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// =========================================================================
// TURKUAZ & BEYAZ MATERIAL 3 COLOR SCHEME (Suno / Udio / Flow Music)
// =========================================================================

private val TurquoiseWhiteColorScheme =
  lightColorScheme(
    primary = StudioCyan,
    onPrimary = Color.White,
    primaryContainer = StudioTurquoiseTint,
    onPrimaryContainer = StudioCyanDark,
    inversePrimary = StudioCyanGlow,

    secondary = StudioCyanDark,
    onSecondary = Color.White,
    secondaryContainer = StudioTurquoiseTint,
    onSecondaryContainer = StudioTextPrimary,

    tertiary = StudioOceanic,
    onTertiary = Color.White,
    tertiaryContainer = StudioCyanLight,
    onTertiaryContainer = StudioTextPrimary,

    background = StudioBackground,
    onBackground = StudioTextPrimary,

    surface = StudioSurface,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = StudioTextSecondary,
    surfaceTint = StudioCyan,
    inverseSurface = StudioTextPrimary,
    inverseOnSurface = StudioSurface,

    surfaceContainerLowest = StudioSurface,
    surfaceContainerLow = StudioSurface,
    surfaceContainer = StudioSurfaceCard,
    surfaceContainerHigh = StudioSurfaceVariant,
    surfaceContainerHighest = StudioTurquoiseTint,

    outline = StudioCardBorder,
    outlineVariant = StudioDivider,
    scrim = Color.Black.copy(alpha = 0.35f),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = TurquoiseWhiteColorScheme,
    typography = Typography,
    content = content
  )
}

