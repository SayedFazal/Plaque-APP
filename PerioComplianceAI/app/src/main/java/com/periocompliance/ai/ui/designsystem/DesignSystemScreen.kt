package com.periocompliance.ai.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.periocompliance.ai.ui.theme.PerioComplianceTheme
import com.periocompliance.ai.ui.theme.PerioTheme
import com.periocompliance.ai.ui.theme.softShadow

/**
 * Not a product screen. This renders every design token so the theme can be checked against the
 * exported PNGs on a real device, in both light and dark, before a single feature is written.
 *
 * It is deleted (along with Routes.DESIGN_SYSTEM) once the app has real screens to look at.
 */
@Composable
fun DesignSystemScreen(modifier: Modifier = Modifier) {
    val spacing = PerioTheme.spacing
    val colors = PerioTheme.colors
    val scheme = MaterialTheme.colorScheme

    Surface(modifier = modifier.fillMaxSize(), color = scheme.background) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(spacing.sectionGap),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = spacing.xxl,
                bottom = spacing.xxl,
            ),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text("PerioCompliance AI", style = MaterialTheme.typography.headlineLarge, color = scheme.onBackground)
                    Text(
                        "Design system — tokens from DESIGN.md",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Section("Type scale — Inter") {
                    TypeRow("Display L / 48", MaterialTheme.typography.displayLarge)
                    TypeRow("Headline L / 32", MaterialTheme.typography.headlineLarge)
                    TypeRow("Headline M / 24", MaterialTheme.typography.headlineMedium)
                    TypeRow("Title L / 20", MaterialTheme.typography.titleLarge)
                    TypeRow("Body L / 16", MaterialTheme.typography.bodyLarge)
                    TypeRow("Body M / 14", MaterialTheme.typography.bodyMedium)
                    TypeRow("Label M / 12", MaterialTheme.typography.labelMedium)
                    TypeRow("Label S / 11", MaterialTheme.typography.labelSmall)
                }
            }

            item {
                Section("Core palette") {
                    SwatchRow("primary", scheme.primary, scheme.onPrimary)
                    SwatchRow("primaryContainer", scheme.primaryContainer, scheme.onPrimaryContainer)
                    SwatchRow("secondary", scheme.secondary, scheme.onSecondary)
                    SwatchRow("tertiary", scheme.tertiary, scheme.onTertiary)
                    SwatchRow("error", scheme.error, scheme.onError)
                }
            }

            item {
                Section("Surface layers") {
                    SwatchRow("surfaceContainerLowest", scheme.surfaceContainerLowest, scheme.onSurface)
                    SwatchRow("surfaceContainerLow", scheme.surfaceContainerLow, scheme.onSurface)
                    SwatchRow("surfaceContainer", scheme.surfaceContainer, scheme.onSurface)
                    SwatchRow("surfaceContainerHigh", scheme.surfaceContainerHigh, scheme.onSurface)
                    SwatchRow("surfaceContainerHighest", scheme.surfaceContainerHighest, scheme.onSurface)
                }
            }

            item {
                Section("Extended — status and charts") {
                    SwatchRow("success", colors.success, colors.onSuccess)
                    SwatchRow("warning", colors.warning, colors.onWarning)
                    SwatchRow("chartTrend", colors.chartTrend, Color.White)
                    SwatchRow("chartHealthy", colors.chartHealthy, Color.White)
                    SwatchRow("chartAtRisk", colors.chartAtRisk, Color.Black)
                }
            }

            item {
                Section("AI gradient") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(colors.aiGradientStart, colors.aiGradientEnd),
                                ),
                                shape = PerioTheme.shapes.card,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "AI-generated",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                    }
                }
            }

            item {
                Section("Card — soft shadow (light) / tonal layer (dark)") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .softShadow(PerioTheme.shapes.card)
                            .background(scheme.surfaceContainerLowest, PerioTheme.shapes.card)
                            .border(spacing.hairline, scheme.outlineVariant, PerioTheme.shapes.card)
                            .padding(spacing.cardPadding),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            Text("Smile Score", style = MaterialTheme.typography.titleMedium, color = scheme.onSurface)
                            Text(
                                "16dp radius, 1px outline, 16dp padding.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                Section("Controls — 8dp radius, 44dp min touch target") {
                    Button(
                        onClick = {},
                        shape = PerioTheme.shapes.button,
                        modifier = Modifier.fillMaxWidth().height(spacing.minTouchTarget),
                    ) { Text("Primary") }
                    Spacer(Modifier.height(spacing.sm))
                    OutlinedButton(
                        onClick = {},
                        shape = PerioTheme.shapes.button,
                        modifier = Modifier.fillMaxWidth().height(spacing.minTouchTarget),
                    ) { Text("Secondary") }
                    Spacer(Modifier.height(spacing.sm))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Email") },
                        shape = PerioTheme.shapes.input,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                Section("Spacing — 8px grid") {
                    SpacingBar("xs", spacing.xs)
                    SpacingBar("sm", spacing.sm)
                    SpacingBar("md", spacing.md)
                    SpacingBar("lg", spacing.lg)
                    SpacingBar("xl", spacing.xl)
                    SpacingBar("xxl", spacing.xxl)
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PerioTheme.spacing.sm)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(PerioTheme.spacing.xs))
        Column(verticalArrangement = Arrangement.spacedBy(PerioTheme.spacing.sm)) {
            content()
        }
    }
}

@Composable
private fun TypeRow(label: String, style: TextStyle) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Healthy gums, tracked daily",
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SwatchRow(name: String, color: Color, onColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PerioTheme.spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color, PerioTheme.shapes.button)
                .border(
                    PerioTheme.spacing.hairline,
                    MaterialTheme.colorScheme.outlineVariant,
                    PerioTheme.shapes.button,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("Aa", style = MaterialTheme.typography.labelMedium, color = onColor)
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SpacingBar(name: String, value: Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PerioTheme.spacing.sm),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp),
        )
        Box(
            modifier = Modifier
                .width(value)
                .height(16.dp)
                .background(MaterialTheme.colorScheme.primary, PerioTheme.shapes.chip),
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 1400)
@Composable
private fun DesignSystemPreviewLight() {
    PerioComplianceTheme(darkTheme = false) { DesignSystemScreen() }
}

@Preview(name = "Dark - Obsidian Deep", showBackground = true, heightDp = 1400)
@Composable
private fun DesignSystemPreviewDark() {
    PerioComplianceTheme(darkTheme = true) { DesignSystemScreen() }
}
