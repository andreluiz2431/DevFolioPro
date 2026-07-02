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
import com.example.data.local.entities.ThemeSettingsEntity

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

fun String.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Color(0xFF3F51B5) // Fallback indigo
    }
}

@Composable
fun DynamicPortfolioTheme(
    settings: ThemeSettingsEntity,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when {
        settings.useSystemTheme -> systemInDark
        else -> settings.isDarkModeForced
    }

    val primary = settings.primaryColorHex.toColor()
    val secondary = settings.secondaryColorHex.toColor()
    
    val colorScheme = if (isDark) {
        // High-fidelity dark aesthetic (slate/charcoal background)
        darkColorScheme(
            primary = primary,
            secondary = secondary,
            background = Color(0xFF121214),
            surface = Color(0xFF1E1E24),
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color(0xFFE2E8F0),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF334155),
            onSurfaceVariant = Color(0xFFCBD5E1)
        )
    } else {
        // High-fidelity light aesthetic
        val bg = settings.backgroundColorHex.toColor()
        val text = settings.textColorHex.toColor()
        lightColorScheme(
            primary = primary,
            secondary = secondary,
            background = bg,
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = text,
            onSurface = text,
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF475569)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
