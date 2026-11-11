package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = AniPrimary,
    onPrimary = Color.White,
    primaryContainer = AniDarkSurfaceVariant,
    onPrimaryContainer = Color(0xFFD6C8FF),
    secondary = AniSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A1A28),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = AniTertiary,
    onTertiary = Color.Black,
    background = AniDarkBackground,
    onBackground = AniTextPrimaryDark,
    surface = AniDarkSurface,
    onSurface = AniTextPrimaryDark,
    surfaceVariant = AniDarkSurfaceVariant,
    onSurfaceVariant = AniTextSecondaryDark,
    outline = AniDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AniPrimaryVariant,
    onPrimary = Color.White,
    primaryContainer = AniLightSurfaceVariant,
    onPrimaryContainer = AniPrimaryVariant,
    secondary = AniSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDE4),
    onSecondaryContainer = Color(0xFF680026),
    tertiary = Color(0xFF00B4D8),
    onTertiary = Color.White,
    background = AniLightBackground,
    onBackground = AniTextPrimaryLight,
    surface = AniLightSurface,
    onSurface = AniTextPrimaryLight,
    surfaceVariant = AniLightSurfaceVariant,
    onSurfaceVariant = AniTextSecondaryLight,
    outline = AniLightCardBorder
)

private val OledColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = AniOledSurfaceVariant,
    onPrimaryContainer = Color(0xFFD6C8FF),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E1E1E),
    onSecondaryContainer = Color(0xFFE0E0E0),
    tertiary = AniGold,
    onTertiary = Color.Black,
    background = AniOledBackground,
    onBackground = Color(0xFFF0F0F0),
    surface = AniOledSurface,
    onSurface = Color(0xFFF0F0F0),
    surfaceVariant = AniOledSurfaceVariant,
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = AniOledBorder
)

private val SakuraColorScheme = lightColorScheme(
    primary = AniSakuraPrimary,
    onPrimary = Color.White,
    primaryContainer = AniSakuraSurfaceVariant,
    onPrimaryContainer = AniSakuraText,
    secondary = AniSakuraSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD1DC),
    onSecondaryContainer = AniSakuraText,
    tertiary = Color(0xFFFFB6C1),
    onTertiary = Color.Black,
    background = AniSakuraBackground,
    onBackground = AniSakuraText,
    surface = AniSakuraSurface,
    onSurface = AniSakuraText,
    surfaceVariant = AniSakuraSurfaceVariant,
    onSurfaceVariant = Color(0xFF884D60),
    outline = Color(0xFFFFC0CB)
)

@Composable
fun AniVexaTheme(
    themeMode: AppThemeMode = AppThemeMode.CYBERPUNK,
    systemDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.CYBERPUNK -> DarkColorScheme
        AppThemeMode.OLED -> OledColorScheme
        AppThemeMode.SAKURA -> SakuraColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (systemDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (systemDarkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
