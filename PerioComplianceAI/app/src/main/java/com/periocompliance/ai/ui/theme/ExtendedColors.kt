package com.periocompliance.ai.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colours the design system calls for that have no Material 3 role to live in:
 * the Health Green accent, the Warning Amber, the chart palette, and the glass overlay surfaces.
 *
 * Provenance matters here, so it is marked per-token:
 *   [DESIGN.md]  - a hex given explicitly in one of the two DESIGN.md files.
 *   [derived]    - not given a hex; inferred to satisfy the written spec and WCAG AA on its
 *                  surface. These are the only colours in the app that are a judgement call.
 *                  If you have the real values, change them here and nothing else.
 */
@Immutable
data class PerioColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    /** Line/area colour for trend series. DESIGN.md: "Primary Blue for trends". */
    val chartTrend: Color,
    /** DESIGN.md: "Accent Green for Healthy". */
    val chartHealthy: Color,
    /** DESIGN.md: "Warning Amber for At-Risk". */
    val chartAtRisk: Color,
    val chartCritical: Color,
    val chartGrid: Color,
    /** Fill for backdrop-blurred overlays (tooltips, floating menus, modals). */
    val glassSurface: Color,
    /** The 1px border that keeps a glass surface legible against content. */
    val glassBorder: Color,
    /** DESIGN.md: soft ambient shadow, 0px 4px 12px rgba(15,23,42,0.05). */
    val shadow: Color,
    /** Gradient used to mark AI-generated content (Primary -> Accent). */
    val aiGradientStart: Color,
    val aiGradientEnd: Color,
)

internal val LightPerioColors = PerioColors(
    success = Color(0xFF00A572),           // [DESIGN.md] Health Green
    onSuccess = Color(0xFFFFFFFF),         // [derived]
    successContainer = Color(0xFFC2F0DE),  // [derived] tint of Health Green
    onSuccessContainer = Color(0xFF00311F),// [DESIGN.md] on-secondary-container (Obsidian)
    warning = Color(0xFFF59E0B),           // [derived] "Warning Amber"
    onWarning = Color(0xFF3A2400),         // [derived]
    warningContainer = Color(0xFFFFEDD5),  // [derived]
    onWarningContainer = Color(0xFF7C2D12),// [derived]
    chartTrend = Color(0xFF2563EB),        // [DESIGN.md] Clinical Blue
    chartHealthy = Color(0xFF10B981),      // [DESIGN.md] Emerald accent
    chartAtRisk = Color(0xFFF59E0B),       // [derived] Warning Amber
    chartCritical = Color(0xFFBA1A1A),     // [DESIGN.md] error
    chartGrid = Color(0xFFE2E8F0),         // [DESIGN.md] 1px border colour
    glassSurface = Color(0xCCFFFFFF),      // [DESIGN.md] 80% opaque white
    glassBorder = Color(0xFFE2E8F0),       // [DESIGN.md] 1px subtle border
    shadow = Color(0xFF0F172A),            // [DESIGN.md] rgba(15,23,42,...)
    aiGradientStart = Color(0xFF2563EB),   // [DESIGN.md] Primary -> Accent
    aiGradientEnd = Color(0xFF10B981),
)

internal val DarkPerioColors = PerioColors(
    success = Color(0xFF4EDEA3),           // [DESIGN.md] secondary (Obsidian)
    onSuccess = Color(0xFF003824),         // [DESIGN.md] on-secondary
    successContainer = Color(0xFF00A572),  // [DESIGN.md] secondary-container
    onSuccessContainer = Color(0xFF00311F),// [DESIGN.md] on-secondary-container
    warning = Color(0xFFFBBF24),           // [derived] amber, lifted for dark surfaces
    onWarning = Color(0xFF3A2400),         // [derived]
    warningContainer = Color(0xFF7C2D12),  // [derived]
    onWarningContainer = Color(0xFFFFEDD5),// [derived]
    chartTrend = Color(0xFF60A5FA),        // [derived] blue lifted for contrast on #051424
    chartHealthy = Color(0xFF10B981),      // [DESIGN.md] Emerald accent
    chartAtRisk = Color(0xFFFBBF24),       // [derived]
    chartCritical = Color(0xFFFFB4AB),     // [DESIGN.md] error (dark)
    chartGrid = Color(0xFF334155),         // [DESIGN.md] Slate 700 border
    glassSurface = Color(0xCC1C2B3C),      // [DESIGN.md] 80% opaque overlay layer
    glassBorder = Color(0xFF334155),       // [DESIGN.md] 1px solid #334155
    shadow = Color(0xFF000000),            // [derived] dark mode leans on tonal layers, not shadow
    aiGradientStart = Color(0xFFB4C5FF),   // [DESIGN.md] Primary -> Accent
    aiGradientEnd = Color(0xFF4EDEA3),
)

val LocalPerioColors = staticCompositionLocalOf { LightPerioColors }

/**
 * The Material 3 "fixed" roles -- colours that stay put across light and dark so a component can
 * keep the same identity on either theme.
 *
 * They live here rather than in ColorScheme because Material 3 1.3.2 has no slots for them; they
 * arrive in ColorScheme in 1.4. When the Compose BOM is bumped past that, delete this and pass the
 * values straight to lightColorScheme/darkColorScheme. Every hex below is verbatim from DESIGN.md.
 */
@Immutable
data class PerioFixedColors(
    val primaryFixed: Color,
    val primaryFixedDim: Color,
    val onPrimaryFixed: Color,
    val onPrimaryFixedVariant: Color,
    val secondaryFixed: Color,
    val secondaryFixedDim: Color,
    val onSecondaryFixed: Color,
    val onSecondaryFixedVariant: Color,
    val tertiaryFixed: Color,
    val tertiaryFixedDim: Color,
    val onTertiaryFixed: Color,
    val onTertiaryFixedVariant: Color,
)

internal val LightPerioFixedColors = PerioFixedColors(
    primaryFixed = LightPrimaryFixed,
    primaryFixedDim = LightPrimaryFixedDim,
    onPrimaryFixed = LightOnPrimaryFixed,
    onPrimaryFixedVariant = LightOnPrimaryFixedVariant,
    secondaryFixed = LightSecondaryFixed,
    secondaryFixedDim = LightSecondaryFixedDim,
    onSecondaryFixed = LightOnSecondaryFixed,
    onSecondaryFixedVariant = LightOnSecondaryFixedVariant,
    tertiaryFixed = LightTertiaryFixed,
    tertiaryFixedDim = LightTertiaryFixedDim,
    onTertiaryFixed = LightOnTertiaryFixed,
    onTertiaryFixedVariant = LightOnTertiaryFixedVariant,
)

internal val DarkPerioFixedColors = PerioFixedColors(
    primaryFixed = DarkPrimaryFixed,
    primaryFixedDim = DarkPrimaryFixedDim,
    onPrimaryFixed = DarkOnPrimaryFixed,
    onPrimaryFixedVariant = DarkOnPrimaryFixedVariant,
    secondaryFixed = DarkSecondaryFixed,
    secondaryFixedDim = DarkSecondaryFixedDim,
    onSecondaryFixed = DarkOnSecondaryFixed,
    onSecondaryFixedVariant = DarkOnSecondaryFixedVariant,
    tertiaryFixed = DarkTertiaryFixed,
    tertiaryFixedDim = DarkTertiaryFixedDim,
    onTertiaryFixed = DarkOnTertiaryFixed,
    onTertiaryFixedVariant = DarkOnTertiaryFixedVariant,
)

val LocalPerioFixedColors = staticCompositionLocalOf { LightPerioFixedColors }
