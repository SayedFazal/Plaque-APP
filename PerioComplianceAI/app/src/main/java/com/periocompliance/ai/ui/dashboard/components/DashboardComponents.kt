package com.periocompliance.ai.ui.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.periocompliance.ai.ui.theme.PerioTheme
import com.periocompliance.ai.ui.theme.softShadow

/**
 * The dashboard's building blocks. They exist for the same reason the auth components do: so the
 * cards on the home screen cannot drift into three slightly different cards. Every colour, radius,
 * gap and elevation below comes from the theme — there is no literal here except icon sizes, which
 * the auth screens size in dp too.
 */

/**
 * The base surface every dashboard card sits on: 16px card radius, a 1px hairline outline so it
 * stays defined on white, and the soft ambient shadow that becomes a no-op under the dark
 * ("Obsidian Deep") theme. Pass [onClick] to make the whole card a touch target.
 */
@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit,
) {
    val shape = PerioTheme.shapes.card
    val border = BorderStroke(PerioTheme.spacing.hairline, MaterialTheme.colorScheme.outlineVariant)
    val base = modifier
        .fillMaxWidth()
        .softShadow(shape)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = color,
            contentColor = contentColor,
            border = border,
            modifier = base,
            content = { content() },
        )
    } else {
        Surface(
            shape = shape,
            color = color,
            contentColor = contentColor,
            border = border,
            modifier = base,
            content = { content() },
        )
    }
}

/** Section label above a group of cards ("Your progress", "Quick actions"). */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(bottom = PerioTheme.spacing.sm),
    )
}

/**
 * One compliance metric: a tinted icon chip, a large value, and a caption. Sized to share a row
 * with its siblings via [Modifier.weight] from the caller.
 */
@Composable
fun StatTile(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    DashboardCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(PerioTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PerioTheme.spacing.xs),
        ) {
            IconChip(icon = icon, tint = tint)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * A navigation entry: leading icon chip, title + subtitle, trailing chevron. The whole card is the
 * touch target.
 */
@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DashboardCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.padding(PerioTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PerioTheme.spacing.md),
        ) {
            IconChip(icon = icon, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A round, tinted container for a leading icon — the recurring motif across the cards. */
@Composable
fun IconChip(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(tint),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(22.dp),
        )
    }
}
