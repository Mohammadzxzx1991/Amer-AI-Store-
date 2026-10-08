package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset

// Space/Cosmic Themed Color Palette
val CosmicDeepBlack = Color(0xFF05050A)
val StarDustWhite = Color(0xFFE0E0E0)
val NebulaPurple = Color(0xFF6A0DAD)
val SupernovaGold = Color(0xFFFFD700)
val GalacticCyan = Color(0xFF00FFFF)
val VoidBlue = Color(0xFF0D0D26)
val CelestialViolet = Color(0xFF4B0082)

val SlateDarkBg: Color
    @Composable
    @ReadOnlyComposable
    get() {
        return CosmicDeepBlack
    }

val CardDarkBg: Color
    @Composable
    @ReadOnlyComposable
    get() {
        return VoidBlue
    }

val PrimaryCyan: Color
    @Composable
    @ReadOnlyComposable
    get() {
        return GalacticCyan
    }

val SecondaryMint: Color
    @Composable
    @ReadOnlyComposable
    get() {
        return CelestialViolet
    }

val AccentCoral: Color
    @Composable
    @ReadOnlyComposable
    get() = SupernovaGold

val WarmAmbar: Color
    @Composable
    @ReadOnlyComposable
    get() = Color(0xFFFFB703) // Radiant Sun Gold

val SoftGrayText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDark.current) Color(0xFF94A3B8) else Color(0xFF64748B)
val HeaderBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = PrimaryCyan

val PolarLight: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = LocalIsDark.current
        return when (LocalAppThemeStyle.current) {
            "gold" -> if (isDark) Color(0xFFFFFBEB) else Color(0xFF0B132B)
            "neon" -> if (isDark) Color(0xFFFAF5FF) else Color(0xFF0C071E)
            "sakura" -> if (isDark) Color(0xFFFFF1F2) else Color(0xFF1A0C16)
            "minimal" -> if (isDark) Color(0xFFF8FAFC) else Color(0xFF0A0F1D)
            else -> if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
        }
    }

// Eye-Catching Accent Palette Constants
val ElectricMint = Color(0xFF00F5A0)
val ElectricCyan = Color(0xFF00E5FF)
val NeonPink = Color(0xFFFF007F)
val SunGold = Color(0xFFFFB703)
val NeonCoral = Color(0xFFFF3366)
val ElectricViolet = Color(0xFFA855F7)

val ButtonGlowWhite: Color = Color(0xFFFFFFFF)

val ButtonGlowShadow: Shadow
    @Composable
    @ReadOnlyComposable
    get() = if (!LocalIsDark.current) {
        Shadow(
            color = Color(0xFFFFFFFF),
            offset = Offset(0f, 0f),
            blurRadius = 16f
        )
    } else {
        Shadow(
            color = Color(0x8800F5A0), // Luminous Neon Mint Aura Shadow
            offset = Offset(0f, 0f),
            blurRadius = 12f
        )
    }

val Purple80 = Color(0xFF00F5A0)
val PurpleGrey80 = Color(0xFF00E5FF)
val Pink80 = Color(0xFFFF3366)

val Purple40 = Color(0xFF00C853)
val PurpleGrey40 = Color(0xFFF9FAFB)
val Pink40 = Color(0xFFFF2E63)
