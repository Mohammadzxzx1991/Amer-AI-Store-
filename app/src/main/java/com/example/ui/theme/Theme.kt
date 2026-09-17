package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// CompositionLocal to track if dark mode is active
val LocalIsDark = staticCompositionLocalOf { false }
val LocalAppThemeStyle = staticCompositionLocalOf { "forest" }

// 1. Forest Oasis (Default)
private val ForestDarkColorScheme = darkColorScheme(
    primary = Color(0xFF10B981),      // Emerald Green
    secondary = Color(0xFFB71C1C),    // Dark Red
    tertiary = Color(0xFF34D399),     // Mint Green
    background = Color(0xFF070B09),   // Obsidian dark green-black background
    surface = Color(0xFF0F1613),      // Dark forest-glass card
    onPrimary = Color(0xFF070B09),    // Dark text on neon green
    onSecondary = Color.White,
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF)
)

private val ForestLightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),      // Vibrant green
    secondary = Color(0xFFB71C1C),    // Dark Red
    tertiary = Color(0xFF34D399),
    background = Color(0xFFF1F5F9),   // Ice white
    surface = Color(0xFFFFFFFF),      // Pure White cards
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

// 2. Royal Gold
private val GoldDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),      // Amber Gold
    secondary = Color(0xFFD97706),
    tertiary = Color(0xFFFBBF24),
    background = Color(0xFF0B0F19),   // Midnight Navy
    surface = Color(0xFF151D30),      // Luxury Navy card
    onPrimary = Color(0xFF0B0F19),
    onSecondary = Color.White,
    onBackground = Color(0xFFFFFBEB),
    onSurface = Color(0xFFFFFBEB)
)

private val GoldLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    secondary = Color(0xFFB71C1C),
    tertiary = Color(0xFFF59E0B),
    background = Color(0xFFFFFBEB),   // Warm cream
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0B0F19),
    onSurface = Color(0xFF0B0F19)
)

// 3. Cyber Neon
private val NeonDarkColorScheme = darkColorScheme(
    primary = Color(0xFFEC4899),      // Neon Pink
    secondary = Color(0xFF06B6D4),    // Cyan
    tertiary = Color(0xFF8B5CF6),     // Violet
    background = Color(0xFF0F071B),   // Cyber black-purple
    surface = Color(0xFF1E0E35),      // Dark violet card
    onPrimary = Color(0xFF0F071B),
    onSecondary = Color.White,
    onBackground = Color(0xFFFAF5FF),
    onSurface = Color(0xFFFAF5FF)
)

private val NeonLightColorScheme = lightColorScheme(
    primary = Color(0xFFDB2777),
    secondary = Color(0xFF0891B2),
    tertiary = Color(0xFF7C3AED),
    background = Color(0xFFFAF5FF),   // Lavender white
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F071B),
    onSurface = Color(0xFF0F071B)
)

// 4. Sakura Pastel
private val SakuraDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF472B6),      // Sakura Pink
    secondary = Color(0xFFFB7185),    // Rose Rose
    tertiary = Color(0xFFFDA4AF),
    background = Color(0xFF1C1318),   // Dark chocolate plum
    surface = Color(0xFF2B1D25),      // Warm plum card
    onPrimary = Color(0xFF1C1318),
    onSecondary = Color.White,
    onBackground = Color(0xFFFFF1F2),
    onSurface = Color(0xFFFFF1F2)
)

private val SakuraLightColorScheme = lightColorScheme(
    primary = Color(0xFFEC4899),
    secondary = Color(0xFFF43F5E),
    tertiary = Color(0xFFF23D5F),
    background = Color(0xFFFFF1F2),   // Blossom pink
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1318),
    onSurface = Color(0xFF1C1318)
)

// 5. Classic Slate Minimalist
private val SlateDarkColorScheme = darkColorScheme(
    primary = Color(0xFF64748B),      // Slate
    secondary = Color(0xFF475569),
    tertiary = Color(0xFF94A3B8),
    background = Color(0xFF0F172A),   // Dark slate background
    surface = Color(0xFF1E293B),      // Slate card
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

private val SlateLightColorScheme = lightColorScheme(
    primary = Color(0xFF475569),
    secondary = Color(0xFF334155),
    tertiary = Color(0xFF64748B),
    background = Color(0xFFF8FAFC),   // Slate light
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    themeStyle: String = "forest",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeStyle) {
        "gold" -> if (darkTheme) GoldDarkColorScheme else GoldLightColorScheme
        "neon" -> if (darkTheme) NeonDarkColorScheme else NeonLightColorScheme
        "sakura" -> if (darkTheme) SakuraDarkColorScheme else SakuraLightColorScheme
        "minimal" -> if (darkTheme) SlateDarkColorScheme else SlateLightColorScheme
        else -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalIsDark provides darkTheme,
        LocalAppThemeStyle provides themeStyle
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
