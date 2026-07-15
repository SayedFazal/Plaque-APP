package com.periocompliance.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = LightInversePrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightSurfaceTint,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = LightScrim,
    surfaceBright = LightSurfaceBright,
    surfaceDim = LightSurfaceDim,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    // The *Fixed roles the export defines have no home in Material 3 1.3.2 -- ColorScheme gains
    // them in 1.4. They are transcribed in Color.kt and exposed via PerioTheme.fixed until then.
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkSurfaceTint,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,
    surfaceBright = DarkSurfaceBright,
    surfaceDim = DarkSurfaceDim,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    // See the note on LightColorScheme: no *Fixed roles until Material 3 1.4.
)

/**
 * The app's only theme. Wrap everything in it, once, at the root.
 *
 * There is deliberately no `dynamicColor` parameter. Material You would let the user's wallpaper
 * repaint a clinical product in whatever the phone picked, which is exactly the drift the exported
 * design is meant to prevent. The brand colours win.
 */
@Composable
fun PerioComplianceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val perioColors = if (darkTheme) DarkPerioColors else LightPerioColors
    val perioFixedColors = if (darkTheme) DarkPerioFixedColors else LightPerioFixedColors
    val perioElevation = if (darkTheme) DarkPerioElevation else LightPerioElevation

    CompositionLocalProvider(
        LocalPerioColors provides perioColors,
        LocalPerioFixedColors provides perioFixedColors,
        LocalPerioSpacing provides PerioSpacing(),
        LocalPerioShapes provides PerioShapeTokens(),
        LocalPerioElevation provides perioElevation,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PerioTypography,
            shapes = PerioShapes,
            content = content,
        )
    }
}

/**
 * Accessor for the tokens Material 3 has no home for.
 *
 *     PerioTheme.colors.success      PerioTheme.spacing.sectionGap
 *     PerioTheme.shapes.card         PerioTheme.elevation.overlay
 *
 * Everything else -- primary, surface, typography, the base shape scale -- comes from
 * MaterialTheme as normal.
 */
object PerioTheme {
    val colors: PerioColors
        @Composable @ReadOnlyComposable get() = LocalPerioColors.current

    val fixed: PerioFixedColors
        @Composable @ReadOnlyComposable get() = LocalPerioFixedColors.current

    val spacing: PerioSpacing
        @Composable @ReadOnlyComposable get() = LocalPerioSpacing.current

    val shapes: PerioShapeTokens
        @Composable @ReadOnlyComposable get() = LocalPerioShapes.current

    val elevation: PerioElevation
        @Composable @ReadOnlyComposable get() = LocalPerioElevation.current
}
