package org.delcom.pam_p5_ifs23012.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Konfigurasi Skema Warna Gelap ---
private val DarkColorScheme = darkColorScheme(
    primary = BrownPrimaryDark,
    onPrimary = OnBrownPrimaryDark,
    primaryContainer = BrownPrimaryContainerDark,
    onPrimaryContainer = OnBrownPrimaryContainerDark,
    secondary = BeigeSecondaryDark,
    onSecondary = OnBeigeSecondaryDark,
    secondaryContainer = BeigeSecondaryContainerDark,
    onSecondaryContainer = OnBeigeSecondaryContainerDark,
    background = FurnitureBackgroundDark,
    surface = FurnitureSurfaceDark,
    onSurface = OnFurnitureSurfaceDark
)

// --- Konfigurasi Skema Warna Terang ---
private val LightColorScheme = lightColorScheme(
    primary = BrownPrimaryLight,
    onPrimary = OnBrownPrimaryLight,
    primaryContainer = BrownPrimaryContainerLight,
    onPrimaryContainer = OnBrownPrimaryContainerLight,
    secondary = BeigeSecondaryLight,
    onSecondary = OnBeigeSecondaryLight,
    secondaryContainer = BeigeSecondaryContainerLight,
    onSecondaryContainer = OnBeigeSecondaryContainerLight,
    background = FurnitureBackgroundLight,
    surface = FurnitureSurfaceLight,
    onSurface = OnFurnitureSurfaceLight
)

@Composable
fun DelcomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Kita matikan agar warna cokelat kita tetap dipakai
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Pastikan Typography.kt masih ada
        content = content
    )
}