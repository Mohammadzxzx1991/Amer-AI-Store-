package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset

// CompositionLocal to track if dark mode is active (defined in Theme.kt)

// Premium Vibrant Organic Grocery Color Scheme (Behance Dark / Specific Light Palette)
val SlateDarkBg: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = LocalIsDark.current
        return when (LocalAppThemeStyle.current) {
            "gold" -> if (isDark) Color(0xFF0B0F19) else Color(0xFFFFFBEB)
            "neon" -> if (isDark) Color(0xFF0F071B) else Color(0xFFFAF5FF)
            "sakura" -> if (isDark) Color(0xFF1C1318) else Color(0xFFFFF1F2)
            "minimal" -> if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
            else -> if (isDark) Color(0xFF070B09) else Color(0xFFF1F5F9) // "forest" / default
        }
    }

val CardDarkBg: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = LocalIsDark.current
        return when (LocalAppThemeStyle.current) {
            "gold" -> if (isDark) Color(0xFF151D30) else Color(0xFFFFFFFF)
            "neon" -> if (isDark) Color(0xFF1E0E35) else Color(0xFFFFFFFF)
            "sakura" -> if (isDark) Color(0xFF2B1D25) else Color(0xFFFFFFFF)
            "minimal" -> if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
            else -> if (isDark) Color(0xFF0F1613) else Color(0xFFFFFFFF)
        }
    }

val PrimaryCyan: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = LocalIsDark.current
        return when (LocalAppThemeStyle.current) {
            "gold" -> if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)
            "neon" -> if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777)
            "sakura" -> if (isDark) Color(0xFFF472B6) else Color(0xFFEC4899)
            "minimal" -> if (isDark) Color(0xFF64748B) else Color(0xFF475569)
            else -> if (isDark) Color(0xFF10B981) else Color(0xFF1D4ED8)
        }
    }

val SecondaryMint: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = LocalIsDark.current
        return when (LocalAppThemeStyle.current) {
            "gold" -> if (isDark) Color(0xFFFBBF24) else Color(0xFFF59E0B)
            "neon" -> if (isDark) Color(0xFF06B6D4) else Color(0xFF0891B2)
            "sakura" -> if (isDark) Color(0xFFFB7185) else Color(0xFFF43F5E)
            "minimal" -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            else -> if (isDark) Color(0xFF34D399) else Color(0xFF2563EB)
        }
    }

val HeaderBlue: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = LocalIsDark.current
        return when (LocalAppThemeStyle.current) {
            "gold" -> if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)
            "neon" -> if (isDark) Color(0xFFEC4899) else Color(0xFFDB2777)
            "sakura" -> if (isDark) Color(0xFFF472B6) else Color(0xFFEC4899)
            "minimal" -> if (isDark) Color(0xFF64748B) else Color(0xFF475569)
            else -> if (isDark) Color(0xFF10B981) else Color(0xFF1D4ED8)
        }
    }

val AccentCoral: Color
    @Composable
    @ReadOnlyComposable
    get() = Color(0xFFF43F5E) // Beautiful coral red

val WarmAmbar: Color
    @Composable
    @ReadOnlyComposable
    get() = Color(0xFFF59E0B) // Amber

val SoftGrayText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDark.current) Color(0xFF94A3B8) else Color(0xFF64748B) // Slate 400 / Slate 500

val PolarLight: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = LocalIsDark.current
        return when (LocalAppThemeStyle.current) {
            "gold" -> if (isDark) Color(0xFFFFFBEB) else Color(0xFF0B0F19)
            "neon" -> if (isDark) Color(0xFFFAF5FF) else Color(0xFF0F071B)
            "sakura" -> if (isDark) Color(0xFFFFF1F2) else Color(0xFF1C1318)
            "minimal" -> if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
            else -> if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
        }
    }

val ButtonGlowWhite: Color = Color(0xFFFFFFFF)

val ButtonGlowShadow: Shadow
    @Composable
    @ReadOnlyComposable
    get() = if (!LocalIsDark.current) {
        Shadow(
            color = Color(0xFFFFFFFF), // Stronger bright glowing white shadow in Light Mode
            offset = Offset(0f, 0f),
            blurRadius = 16f
        )
    } else {
        Shadow(
            color = Color(0x88FFFFFF),
            offset = Offset(0f, 0f),
            blurRadius = 8f
        )
    }

val Purple80 = Color(0xFFB4E3C5)
val PurpleGrey80 = Color(0xFFC2DCD2)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF53B175)
val PurpleGrey40 = Color(0xFFF9FAFB)
val Pink40 = Color(0xFFF23D5F)



