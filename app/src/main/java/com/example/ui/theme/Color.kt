package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// LIQUID GLASS DESIGN SYSTEM — PANDA AI AGENT
// ============================================================

// === Deep Space Dark Backgrounds ===
val DeepSpace        = Color(0xFF050A18)   // Primary deep navy space background
val NavyDark         = Color(0xFF0A1424)   // Inner glass panel deep background
val NavyMid          = Color(0xFF0E1E34)   // Secondary drawer/surface
val DarkCardBg       = Color(0xFF10213E)   // Stronger card definition

// === Frosted Glass Layers (alpha overlays) ===
val GlassWhite       = Color(0x1BFFFFFF)   // ~10-15% white — glass panel base fill
val GlassWhiteLight  = Color(0x2EFFFFFF)   // ~18% white — higher contrast glass fill
val GlassBorder      = Color(0x33FFFFFF)   // 20% white — frosted edge border
val GlassBorderBold  = Color(0x52FFFFFF)   // 32% white — highlighted border
val GlassHighlight   = Color(0x0CFFFFFF)   // Subtle inner sheen overlay

// === Vibrant Neon Glow Accents ===
val CyanGlow         = Color(0xFF00D4FF)   // Electrifying screen highlights / active state
val PurpleAccent     = Color(0xFF8B5CF6)   // Cyber purple accent / pro indicators
val BlueAccent       = Color(0xFF3B82F6)   // Tech blue accent
val NeonPink         = Color(0xFFEC4899)   // Coral pink accents
val SoftAmber        = Color(0xFFF59E0B)   // Attention grabbing details

// === CTA / Button Gradients ===
val GradientStart    = Color(0xFF6366F1)   // Bright indigo
val GradientEnd      = Color(0xFF8B5CF6)   // Glowing violet

// === Text Elements ===
val TextPrimary      = Color(0xFFFFFFFF)
val TextSecondary    = Color(0xD9FFFFFF)   // 85% white
val TextMuted        = Color(0x80FFFFFF)   // 50% white
val TextSubtle       = Color(0x4DFFFFFF)   // 30% white

// === Status Indicators ===
val StatusGreen      = Color(0xFF10B981)
val StatusAmber      = Color(0xFFF59E0B)
val StatusRed        = Color(0xFFEF4444)

// === Legacy / Compatibility Aliases ===
val JarvisPrimary         = GradientStart
val JarvisSecondary       = CyanGlow
val JarvisAccent          = PurpleAccent
val JarvisSuccess         = StatusGreen
val JarvisWarning         = StatusAmber
val JarvisError           = StatusRed
val JarvisOrange          = Color(0xFFF97316)
val DarkBackground        = DeepSpace
val DarkSurface           = NavyDark
val DarkSurfaceVariant    = NavyMid
val DarkCard              = DarkCardBg
val DarkOnSurface         = TextPrimary
val DarkOnSurfaceVariant  = TextSecondary

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
