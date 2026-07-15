package com.periocompliance.ai.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Depth model. The two design systems disagree on purpose, and the app honours both:
 *
 *   Light ("PerioCompliance AI"): ambient shadows. Cards get 0px 4px 12px rgba(15,23,42,0.05).
 *   Dark  ("Obsidian Deep"):      "depth is communicated through Tonal Layers rather than heavy
 *                                 shadows... Do not use shadows; let the tonal difference create
 *                                 the separation."
 *
 * So [PerioElevation.useShadows] is false in dark, and [softShadow] becomes a no-op there. Cards
 * lean on surfaceContainer + a 1px outline instead. Call [softShadow] unconditionally; the theme
 * decides whether it draws.
 */
@Immutable
data class PerioElevation(
    val none: Dp = 0.dp,
    /** Cards and other surface-layer containers. Approximates the 12px blur / 4px y-offset spec. */
    val card: Dp = 8.dp,
    /** Modals, dropdowns, floating menus -- the overlay layer. */
    val overlay: Dp = 16.dp,
    /** DESIGN.md: "Buttons lift slightly on hover, increasing the shadow spread." */
    val buttonResting: Dp = 0.dp,
    val buttonPressed: Dp = 2.dp,
    val useShadows: Boolean = true,
)

internal val LightPerioElevation = PerioElevation(useShadows = true)
internal val DarkPerioElevation = PerioElevation(useShadows = false)

val LocalPerioElevation = staticCompositionLocalOf { LightPerioElevation }

/** The card shadow from DESIGN.md. No-op under Obsidian Deep, by design. */
@Composable
fun Modifier.softShadow(shape: Shape): Modifier {
    val elevation = LocalPerioElevation.current
    val colors = LocalPerioColors.current
    if (!elevation.useShadows) return this
    return shadow(
        elevation = elevation.card,
        shape = shape,
        clip = false,
        ambientColor = colors.shadow.copy(alpha = 0.05f),
        spotColor = colors.shadow.copy(alpha = 0.10f),
    )
}

/** Heavier ambient shadow for the overlay layer (modals, dropdowns). */
@Composable
fun Modifier.overlayShadow(shape: Shape): Modifier {
    val elevation = LocalPerioElevation.current
    val colors = LocalPerioColors.current
    if (!elevation.useShadows) return this
    return shadow(
        elevation = elevation.overlay,
        shape = shape,
        clip = false,
        ambientColor = colors.shadow.copy(alpha = 0.08f),
        spotColor = colors.shadow.copy(alpha = 0.16f),
    )
}
