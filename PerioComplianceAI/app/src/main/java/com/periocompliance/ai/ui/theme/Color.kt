package com.periocompliance.ai.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Colour tokens transcribed from the exported design systems.
 *
 * Light  -> stitch_periocompliance_ai_dental_app/periocompliance_ai/DESIGN.md  ("PerioCompliance AI")
 * Dark   -> stitch_periocompliance_ai_dental_app/obsidian_deep/DESIGN.md       ("Obsidian Deep")
 *
 * These are the single source of truth. Do not hardcode a hex value anywhere else in the app --
 * read colours from MaterialTheme.colorScheme or from LocalPerioColors.
 */

// region Light -- "PerioCompliance AI"

internal val LightPrimary = Color(0xFF004AC6)
internal val LightOnPrimary = Color(0xFFFFFFFF)
internal val LightPrimaryContainer = Color(0xFF2563EB)
internal val LightOnPrimaryContainer = Color(0xFFEEEFFF)
internal val LightInversePrimary = Color(0xFFB4C5FF)

internal val LightSecondary = Color(0xFF565E74)
internal val LightOnSecondary = Color(0xFFFFFFFF)
internal val LightSecondaryContainer = Color(0xFFDAE2FD)
internal val LightOnSecondaryContainer = Color(0xFF5C647A)

internal val LightTertiary = Color(0xFF943700)
internal val LightOnTertiary = Color(0xFFFFFFFF)
internal val LightTertiaryContainer = Color(0xFFBC4800)
internal val LightOnTertiaryContainer = Color(0xFFFFEDE6)

internal val LightError = Color(0xFFBA1A1A)
internal val LightOnError = Color(0xFFFFFFFF)
internal val LightErrorContainer = Color(0xFFFFDAD6)
internal val LightOnErrorContainer = Color(0xFF93000A)

internal val LightBackground = Color(0xFFFAF8FF)
internal val LightOnBackground = Color(0xFF191B23)
internal val LightSurface = Color(0xFFFAF8FF)
internal val LightOnSurface = Color(0xFF191B23)
internal val LightSurfaceVariant = Color(0xFFE1E2ED)
internal val LightOnSurfaceVariant = Color(0xFF434655)

internal val LightSurfaceDim = Color(0xFFD9D9E5)
internal val LightSurfaceBright = Color(0xFFFAF8FF)
internal val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
internal val LightSurfaceContainerLow = Color(0xFFF3F3FE)
internal val LightSurfaceContainer = Color(0xFFEDEDF9)
internal val LightSurfaceContainerHigh = Color(0xFFE7E7F3)
internal val LightSurfaceContainerHighest = Color(0xFFE1E2ED)

internal val LightOutline = Color(0xFF737686)
internal val LightOutlineVariant = Color(0xFFC3C6D7)
internal val LightInverseSurface = Color(0xFF2E3039)
internal val LightInverseOnSurface = Color(0xFFF0F0FB)
internal val LightSurfaceTint = Color(0xFF0053DB)
internal val LightScrim = Color(0xFF000000)

internal val LightPrimaryFixed = Color(0xFFDBE1FF)
internal val LightPrimaryFixedDim = Color(0xFFB4C5FF)
internal val LightOnPrimaryFixed = Color(0xFF00174B)
internal val LightOnPrimaryFixedVariant = Color(0xFF003EA8)
internal val LightSecondaryFixed = Color(0xFFDAE2FD)
internal val LightSecondaryFixedDim = Color(0xFFBEC6E0)
internal val LightOnSecondaryFixed = Color(0xFF131B2E)
internal val LightOnSecondaryFixedVariant = Color(0xFF3F465C)
internal val LightTertiaryFixed = Color(0xFFFFDBCD)
internal val LightTertiaryFixedDim = Color(0xFFFFB596)
internal val LightOnTertiaryFixed = Color(0xFF360F00)
internal val LightOnTertiaryFixedVariant = Color(0xFF7D2D00)

// endregion

// region Dark -- "Obsidian Deep"

internal val DarkPrimary = Color(0xFFB4C5FF)
internal val DarkOnPrimary = Color(0xFF002A78)
internal val DarkPrimaryContainer = Color(0xFF2563EB)
internal val DarkOnPrimaryContainer = Color(0xFFEEEFFF)
internal val DarkInversePrimary = Color(0xFF0053DB)

internal val DarkSecondary = Color(0xFF4EDEA3)
internal val DarkOnSecondary = Color(0xFF003824)
internal val DarkSecondaryContainer = Color(0xFF00A572)
internal val DarkOnSecondaryContainer = Color(0xFF00311F)

internal val DarkTertiary = Color(0xFFFFB596)
internal val DarkOnTertiary = Color(0xFF581E00)
internal val DarkTertiaryContainer = Color(0xFFBC4800)
internal val DarkOnTertiaryContainer = Color(0xFFFFEDE6)

internal val DarkError = Color(0xFFFFB4AB)
internal val DarkOnError = Color(0xFF690005)
internal val DarkErrorContainer = Color(0xFF93000A)
internal val DarkOnErrorContainer = Color(0xFFFFDAD6)

internal val DarkBackground = Color(0xFF051424)
internal val DarkOnBackground = Color(0xFFD4E4FA)
internal val DarkSurface = Color(0xFF051424)
internal val DarkOnSurface = Color(0xFFD4E4FA)
internal val DarkSurfaceVariant = Color(0xFF273647)
internal val DarkOnSurfaceVariant = Color(0xFFC3C6D7)

internal val DarkSurfaceDim = Color(0xFF051424)
internal val DarkSurfaceBright = Color(0xFF2C3A4C)
internal val DarkSurfaceContainerLowest = Color(0xFF010F1F)
internal val DarkSurfaceContainerLow = Color(0xFF0D1C2D)
internal val DarkSurfaceContainer = Color(0xFF122131)
internal val DarkSurfaceContainerHigh = Color(0xFF1C2B3C)
internal val DarkSurfaceContainerHighest = Color(0xFF273647)

internal val DarkOutline = Color(0xFF8D90A0)
internal val DarkOutlineVariant = Color(0xFF434655)
internal val DarkInverseSurface = Color(0xFFD4E4FA)
internal val DarkInverseOnSurface = Color(0xFF233143)
internal val DarkSurfaceTint = Color(0xFFB4C5FF)
internal val DarkScrim = Color(0xFF000000)

internal val DarkPrimaryFixed = Color(0xFFDBE1FF)
internal val DarkPrimaryFixedDim = Color(0xFFB4C5FF)
internal val DarkOnPrimaryFixed = Color(0xFF00174B)
internal val DarkOnPrimaryFixedVariant = Color(0xFF003EA8)
internal val DarkSecondaryFixed = Color(0xFF6FFBBE)
internal val DarkSecondaryFixedDim = Color(0xFF4EDEA3)
internal val DarkOnSecondaryFixed = Color(0xFF002113)
internal val DarkOnSecondaryFixedVariant = Color(0xFF005236)
internal val DarkTertiaryFixed = Color(0xFFFFDBCD)
internal val DarkTertiaryFixedDim = Color(0xFFFFB596)
internal val DarkOnTertiaryFixed = Color(0xFF360F00)
internal val DarkOnTertiaryFixedVariant = Color(0xFF7D2D00)

// endregion
