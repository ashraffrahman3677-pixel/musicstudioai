package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// FROSTED GLASS THEME PALETTE
// ==========================================

// Atmospheric Canvas & Orbs
val FrostedCanvasBackground = Color(0xFFF3F0F8)
val FrostedOrbLilac = Color(0xFFD0BCFF)
val FrostedOrbLavender = Color(0xFFEADDFF)
val FrostedOrbViolet = Color(0xFFC8B6FF)

// Frosted Glass Translucent Surfaces
val FrostedGlassSurface = Color(0x66FFFFFF)          // ~40% white glass
val FrostedGlassSurfaceVariant = Color(0x80FFFFFF)   // ~50% white glass
val FrostedGlassCard = Color(0x73FFFFFF)             // ~45% white glass
val FrostedGlassCardElevated = Color(0xA6FFFFFF)     // ~65% white glass
val FrostedGlassInput = Color(0x59FFFFFF)            // ~35% white glass
val FrostedGlassNavBar = Color(0xD9F3F0F8)           // ~85% frosted backdrop for bottom nav

// Frosted Crystalline Borders & Highlights
val FrostedBorder = Color(0x80FFFFFF)               // 50% crisp white highlight border
val FrostedBorderSubtle = Color(0x40FFFFFF)         // 25% subtle white highlight border
val FrostedBorderAccent = Color(0x336750A4)         // 20% violet accent border
val FrostedShadow = Color(0x0D6750A4)               // Soft purple-tinted ambient shadow

// Brand Primary Accents (Regal Violet & Lavender)
val FrostedPrimary = Color(0xFF6750A4)              // Signature deep violet
val FrostedPrimaryDark = Color(0xFF21005D)          // Deep plum/violet for high-contrast pills
val FrostedPrimaryLight = Color(0xFFEADDFF)         // Pale lavender container
val FrostedPrimaryMedium = Color(0xFFD0BCFF)        // Radiant soft lilac

// Secondary & Accent Colors
val FrostedSecondary = Color(0xFF625B71)
val FrostedSecondaryContainer = Color(0xFFE8DEF8)
val FrostedTertiary = Color(0xFF7D5260)
val FrostedTertiaryContainer = Color(0xFFFFD8E4)

// Vibrant Accent Highlights
val FrostedAccentCyan = Color(0xFF0284C7)           // Sky Cyan
val FrostedAccentRose = Color(0xFFDB2777)           // Rose Magenta
val FrostedAccentAmber = Color(0xFFD97706)          // Warm Amber
val FrostedAccentEmerald = Color(0xFF15803D)        // Emerald Green
val FrostedAccentCrimson = Color(0xFFDC2626)        // Crimson Red

// High-Contrast Glass Typography Colors
val FrostedTextPrimary = Color(0xFF0F172A)          // Slate 900 (crisp, readable on frosted glass)
val FrostedTextSecondary = Color(0xFF475569)        // Slate 600
val FrostedTextMuted = Color(0xFF64748B)            // Slate 500
val FrostedTextDisabled = Color(0xFF94A3B8)         // Slate 400

// Compatibility Aliases to seamlessly support all studio components
val StudioBackground = FrostedCanvasBackground
val StudioSurface = FrostedGlassSurface
val StudioSurfaceVariant = FrostedGlassSurfaceVariant
val StudioCardBackground = FrostedGlassCard
val StudioBorder = FrostedBorder

val ElectricViolet = FrostedPrimary
val NeonCyan = FrostedAccentCyan
val SunsetPink = FrostedAccentRose
val CyberEmerald = FrostedAccentEmerald
val AmberGlow = FrostedAccentAmber
val CrimsonAlert = FrostedAccentCrimson

val TextPrimary = FrostedTextPrimary
val TextSecondary = FrostedTextSecondary
val TextMuted = FrostedTextMuted

val PrimaryGradientStart = Color(0xFF6750A4)
val PrimaryGradientEnd = Color(0xFF21005D)
val CyanGradientEnd = Color(0xFF7C3AED)
