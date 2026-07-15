package com.periocompliance.ai.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale from DESIGN.md. The rule the export states, and that every screen must hold to:
 * "All margins, paddings, and component heights must be multiples of 8 (with 4px used for
 * internal atomic spacing)."
 *
 * The semantic tokens below the raw scale exist so a screen never invents a number:
 * card padding is `md`, the gap between sections is `lg`, the screen gutter is `screenMargin`.
 */
@Immutable
data class PerioSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val xxxl: Dp = 64.dp,

    /** DESIGN.md: "Mobile: elements reflow to a single column with 16px side margins." */
    val screenMargin: Dp = 16.dp,
    /** DESIGN.md: "Use md (16px) for internal card padding." */
    val cardPadding: Dp = 16.dp,
    /** DESIGN.md: "lg (24px) for spacing between major sections." */
    val sectionGap: Dp = 24.dp,
    /** Gap between items inside one component (label + input). */
    val itemGap: Dp = 8.dp,
    /** DESIGN.md: "Large touch targets (min 44px) are mandatory." */
    val minTouchTarget: Dp = 44.dp,
    /** DESIGN.md: cards and inputs carry a 1px border to stay defined on white. */
    val hairline: Dp = 1.dp,
    /** DESIGN.md: focus ring is 2px primary with a 2px offset. */
    val focusRing: Dp = 2.dp,
)

val LocalPerioSpacing = staticCompositionLocalOf { PerioSpacing() }
