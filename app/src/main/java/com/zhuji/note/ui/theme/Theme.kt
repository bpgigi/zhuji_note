package com.zhuji.note.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.zhuji.note.data.local.preferences.ThemeMode

val LightColors = lightColorScheme(
    primary = ClaudeCoral, onPrimary = Color.White,
    primaryContainer = ClaudeSurfaceSoft, onPrimaryContainer = ClaudeBody,
    secondary = ClaudeAccentSky, onSecondary = Color.White,
    secondaryContainer = ClaudeSurfaceCard, onSecondaryContainer = ClaudeBodyStrong,
    tertiary = ClaudeAccentTeal, onTertiary = Color.White,
    tertiaryContainer = ClaudeHairline, onTertiaryContainer = ClaudeInk,
    background = ClaudeCanvas, onBackground = ClaudeInk,
    surface = ClaudeCanvas, onSurface = ClaudeInk,
    surfaceVariant = ClaudeSurfaceSoft, onSurfaceVariant = ClaudeMuted,
    outline = ClaudeMidGray, outlineVariant = ClaudeHairline,
    error = ErrorRed, onError = Color.White,
    errorContainer = ErrorRedContainer, onErrorContainer = Color(0xFF410002),
    inverseSurface = ClaudeInk, inverseOnSurface = ClaudeCanvas, inversePrimary = ClaudeClay,
    scrim = Color.Black,
)

val DarkColors = darkColorScheme(
    primary = WindsurfNeon, onPrimary = WindsurfDarkTeal,
    primaryContainer = ClaudeSurfaceDarkElev, onPrimaryContainer = WindsurfNeon,
    secondary = WindsurfMagenta, onSecondary = ClaudeInk,
    secondaryContainer = ClaudeSurfaceDarkSoft, onSecondaryContainer = WindsurfMagenta,
    tertiary = ClaudeClay, onTertiary = Color.White,
    tertiaryContainer = ClaudeBody, onTertiaryContainer = ClaudeSurfaceSoft,
    background = WindsurfCharcoal, onBackground = ClaudeCanvas,
    surface = WindsurfCharcoal, onSurface = ClaudeCanvas,
    surfaceVariant = ClaudeSurfaceDark, onSurfaceVariant = ClaudeMidGray,
    outline = ClaudeMuted, outlineVariant = ClaudeBody,
    error = ErrorRedDark, onError = Color(0xFF690005),
    errorContainer = ErrorRedDarkContainer, onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = ClaudeCanvas, inverseOnSurface = ClaudeInk, inversePrimary = ClaudeCoral,
    scrim = Color.Black,
)

@Composable
fun ZhujiTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val ctx = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            window.statusBarColor = scheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }
    }

    MaterialTheme(colorScheme = scheme, typography = ZhujiTypography, shapes = ZhujiShapes, content = content)
}
