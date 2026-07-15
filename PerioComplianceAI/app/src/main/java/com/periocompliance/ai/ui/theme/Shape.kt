package com.periocompliance.ai.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shape scale from DESIGN.md `rounded` (rem -> dp at the 16px root the export assumes).
 *
 *   sm 0.25rem -> 4dp     DEFAULT 0.5rem -> 8dp     md 0.75rem -> 12dp
 *   lg 1rem    -> 16dp    xl 1.5rem      -> 24dp    full -> pill
 */
val PerioShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/**
 * The shape rules DESIGN.md states in prose, named so a screen never has to pick a radius:
 * "Buttons & Inputs: 8px. Cards & Modals: 16px. Chips/Badges: fully rounded pill."
 */
@Immutable
data class PerioShapeTokens(
    val button: Shape = RoundedCornerShape(8.dp),
    val input: Shape = RoundedCornerShape(8.dp),
    val card: Shape = RoundedCornerShape(16.dp),
    val modal: Shape = RoundedCornerShape(16.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    val chip: Shape = CircleShape,
    val avatar: Shape = CircleShape,
)

val LocalPerioShapes = staticCompositionLocalOf { PerioShapeTokens() }
