package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// CompositionLocal to track if dark mode is active
val LocalIsDark = staticCompositionLocalOf { false }
val LocalAppThemeStyle = staticCompositionLocalOf { "forest" }

// 1. Luminous Aurora & Electric Mint (Default - أخضر زمردي مشع مع نيون كورال)
private val ForestDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00F5A0),      // Radiant Electric Mint Glow
    secondary = Color(0xFFFF3366),    // Vivid Neon Sunset Coral
    tertiary = Color(0xFF00E5FF),     // Hyper Electric Cyan
    background = Color(0xFF080D1A),   // Cosmic Deep Obsidian Canvas
    surface = Color(0xFF0F1B2B),      // Glassy Midnight Slate Card
    onPrimary = Color(0xFF080D1A),    // High-contrast deep dark on neon mint
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF162338),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF00F5A0)
)

private val ForestLightColorScheme = lightColorScheme(
    primary = Color(0xFF00C853),      // Ultra-vibrant Emerald Green
    secondary = Color(0xFFFF3366),    // Neon Sunset Coral Punch
    tertiary = Color(0xFF00B4D8),     // Electric Sky Blue
    background = Color(0xFFF6FBF7),   // Crisp Clean Pearl Canvas
    surface = Color(0xFFFFFFFF),      // Pure White Cards
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF0FDF4),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFF00C853)
)

// 2. Imperial 24K Gold & Champagne (ذهب ملكي متألق)
private val GoldDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB703),      // Luminous 24K Radiant Gold
    secondary = Color(0xFFFF2E63),    // Vivid Royal Ruby Rose
    tertiary = Color(0xFFFFD166),     // Champagne Amber Glow
    background = Color(0xFF0B132B),   // Midnight Sapphire Black
    surface = Color(0xFF14213D),      // Luxury Royal Navy Card
    onPrimary = Color(0xFF0B132B),
    onSecondary = Color.White,
    onBackground = Color(0xFFFFFBEB),
    onSurface = Color(0xFFFFFBEB),
    surfaceVariant = Color(0xFF1F2F52),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFFFFB703)
)

private val GoldLightColorScheme = lightColorScheme(
    primary = Color(0xFFF59E0B),      // Rich Amber Gold
    secondary = Color(0xFFFF2E63),    // Ruby Rose
    tertiary = Color(0xFFFBBF24),
    background = Color(0xFFFFFBEB),   // Warm Champagne Cream
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0B132B),
    onSurface = Color(0xFF0B132B),
    surfaceVariant = Color(0xFFFEF3C7),
    onSurfaceVariant = Color(0xFF78350F),
    outline = Color(0xFFF59E0B)
)

// 3. Cyber Synthwave & Ultraviolet (نيون سايبر فائق التوهج)
private val NeonDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF007F),      // Hyper Neon Hot Magenta Pink
    secondary = Color(0xFF00F0FF),    // Electric Cyber Cyan
    tertiary = Color(0xFFA855F7),     // Ultraviolet Purple
    background = Color(0xFF0C071E),   // Deep Galactic Void
    surface = Color(0xFF191038),      // Translucent Cyber Indigo Card
    onPrimary = Color(0xFF0C071E),
    onSecondary = Color(0xFF0C071E),
    onBackground = Color(0xFFFAF5FF),
    onSurface = Color(0xFFFAF5FF),
    surfaceVariant = Color(0xFF281954),
    onSurfaceVariant = Color(0xFFE9D5FF),
    outline = Color(0xFFFF007F)
)

private val NeonLightColorScheme = lightColorScheme(
    primary = Color(0xFFE11D48),      // Vivid Crimson Pink
    secondary = Color(0xFF0891B2),    // Deep Cyber Cyan
    tertiary = Color(0xFF7C3AED),     // Electric Violet
    background = Color(0xFFFAF5FF),   // Lavender Crisp Light
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F071B),
    onSurface = Color(0xFF0F071B),
    surfaceVariant = Color(0xFFF3E8FF),
    onSurfaceVariant = Color(0xFF581C87),
    outline = Color(0xFFE11D48)
)

// 4. Sakura Radiant Blossom (ساكورا مشرقة ونابضة)
private val SakuraDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF4081),      // Vivid Sakura Blossom Pink
    secondary = Color(0xFFFF6E40),    // Sunset Peach Glow
    tertiary = Color(0xFFFF80AB),     // Luminous Rose
    background = Color(0xFF1A0C16),   // Deep Plum Obsidian
    surface = Color(0xFF2E1528),      // Velvet Wine Card
    onPrimary = Color(0xFF1A0C16),
    onSecondary = Color.White,
    onBackground = Color(0xFFFFF1F2),
    onSurface = Color(0xFFFFF1F2),
    surfaceVariant = Color(0xFF421E3B),
    onSurfaceVariant = Color(0xFFFECDD3),
    outline = Color(0xFFFF4081)
)

private val SakuraLightColorScheme = lightColorScheme(
    primary = Color(0xFFF43F5E),      // Bright Coral Rose
    secondary = Color(0xFFFF6E40),    // Sunset Peach
    tertiary = Color(0xFFFB7185),
    background = Color(0xFFFFF1F2),   // Blossom Light
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1318),
    onSurface = Color(0xFF1C1318),
    surfaceVariant = Color(0xFFFFE4E6),
    onSurfaceVariant = Color(0xFF881337),
    outline = Color(0xFFF43F5E)
)

// 5. Electric Sapphire & Tech Cobalt (أزرق سفير كهربائي لافت)
private val SlateDarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),      // Electric Sky Sapphire
    secondary = Color(0xFF818CF8),    // Luminous Indigo Purple
    tertiary = Color(0xFF00F5A0),     // Electric Mint Accent
    background = Color(0xFF0A0F1D),   // Deep Space Obsidian
    surface = Color(0xFF131B2E),      // High-tech Cobalt Slate Card
    onPrimary = Color(0xFF0A0F1D),
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E2B47),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF38BDF8)
)

private val SlateLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),      // Brilliant Ocean Cobalt
    secondary = Color(0xFF4F46E5),    // Royal Electric Indigo
    tertiary = Color(0xFF059669),     // Vivid Emerald
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFF0284C7)
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
