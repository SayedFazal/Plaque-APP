package com.periocompliance.ai.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periocompliance.ai.R
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.DashboardSummary
import com.periocompliance.ai.domain.model.DashboardUser
import com.periocompliance.ai.domain.model.ProgressSnapshot
import com.periocompliance.ai.ui.auth.components.messageRes
import com.periocompliance.ai.ui.dashboard.components.DashboardCard
import com.periocompliance.ai.ui.dashboard.components.IconChip
import com.periocompliance.ai.ui.dashboard.components.QuickActionCard
import com.periocompliance.ai.ui.dashboard.components.SectionHeader
import com.periocompliance.ai.ui.dashboard.components.StatTile
import com.periocompliance.ai.ui.theme.PerioComplianceTheme
import com.periocompliance.ai.ui.theme.PerioTheme
import java.time.LocalTime

/**
 * Module 2 — the patient home dashboard.
 *
 * The one screen a verified user lands on. Its real data is the signed-in profile; the compliance
 * metrics render their get-started state until Module 3 starts producing scans. Navigation out of
 * here is entirely via the hoisted callbacks, so this screen owns no routes.
 */
@Composable
fun HomeDashboardScreen(
    onStartScan: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: HomeDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeDashboardContent(
        state = state,
        onRefresh = viewModel::refresh,
        onStartScan = onStartScan,
        onOpenHistory = onOpenHistory,
        onOpenProgress = onOpenProgress,
        onOpenNotifications = onOpenNotifications,
        onSignOut = {
            viewModel.onSignOut()
            onSignedOut()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeDashboardContent(
    state: HomeDashboardUiState,
    onRefresh: () -> Unit,
    onStartScan: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSignOut: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.isRefreshing) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.dashboard_cd_refresh),
                        )
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.dashboard_cd_sign_out),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val summary = state.summary
            when {
                summary != null -> DashboardList(
                    summary = summary,
                    onStartScan = onStartScan,
                    onOpenHistory = onOpenHistory,
                    onOpenProgress = onOpenProgress,
                    onOpenNotifications = onOpenNotifications,
                )

                state.isLoading -> LoadingState()

                else -> ErrorState(
                    error = state.error ?: AuthError.Unknown(null),
                    onRetry = onRefresh,
                )
            }

            // A refresh over content already on screen shows a quiet top indicator, never a blank page.
            if (state.isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = PerioTheme.spacing.sm)
                        .size(24.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Composable
private fun DashboardList(
    summary: DashboardSummary,
    onStartScan: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val spacing = PerioTheme.spacing
    val actions = quickActions(onOpenHistory, onOpenProgress, onOpenNotifications)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = spacing.screenMargin,
            end = spacing.screenMargin,
            top = spacing.sm,
            bottom = spacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.sectionGap),
    ) {
        item { GreetingHeader(user = summary.user) }

        item { HeroScanCard(onStartScan = onStartScan) }

        item {
            Column {
                SectionHeader(stringResource(R.string.dashboard_progress_title))
                ProgressSection(progress = summary.progress)
            }
        }

        item { SectionHeader(stringResource(R.string.dashboard_actions_title)) }

        items(actions) { action ->
            QuickActionCard(
                icon = action.icon,
                title = stringResource(action.titleRes),
                subtitle = stringResource(action.subtitleRes),
                trailingIcon = Icons.Filled.ChevronRight,
                onClick = action.onClick,
            )
        }
    }
}

@Composable
private fun GreetingHeader(user: DashboardUser) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PerioTheme.spacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(greetingRes()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = user.firstName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.dashboard_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Avatar(initial = user.firstName.firstOrNull()?.uppercaseChar()?.toString().orEmpty())
    }
}

@Composable
private fun Avatar(initial: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun HeroScanCard(onStartScan: () -> Unit) {
    DashboardCard(
        onClick = onStartScan,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(PerioTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PerioTheme.spacing.md),
        ) {
            IconChip(icon = Icons.Filled.PhotoCamera, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_hero_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(R.string.dashboard_hero_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun ProgressSection(progress: ProgressSnapshot) {
    if (progress.hasActivity) {
        Row(horizontalArrangement = Arrangement.spacedBy(PerioTheme.spacing.md)) {
            StatTile(
                icon = Icons.Filled.LocalFireDepartment,
                value = progress.streakDays.toString(),
                label = stringResource(R.string.dashboard_stat_streak),
                tint = PerioTheme.colors.warning,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                icon = Icons.Filled.PhotoCamera,
                value = progress.scansCompleted.toString(),
                label = stringResource(R.string.dashboard_stat_scans),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                icon = Icons.Filled.Verified,
                value = progress.complianceScore
                    ?.let { stringResource(R.string.dashboard_compliance_value, it) }
                    ?: stringResource(R.string.dashboard_stat_compliance_empty),
                label = stringResource(R.string.dashboard_stat_compliance),
                tint = PerioTheme.colors.success,
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        EmptyProgressCard()
    }
}

@Composable
private fun EmptyProgressCard() {
    DashboardCard {
        Row(
            modifier = Modifier.padding(PerioTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PerioTheme.spacing.md),
        ) {
            IconChip(icon = Icons.Filled.Insights, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_progress_empty_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.dashboard_progress_empty_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(error: AuthError, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PerioTheme.spacing.screenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(PerioTheme.spacing.md))
        Text(
            text = stringResource(R.string.dashboard_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PerioTheme.spacing.xs))
        Text(
            text = stringResource(error.messageRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PerioTheme.spacing.lg))
        Button(
            onClick = onRetry,
            shape = PerioTheme.shapes.button,
            modifier = Modifier.height(PerioTheme.spacing.minTouchTarget),
        ) {
            Text(stringResource(R.string.dashboard_retry))
        }
    }
}

/** Time-of-day greeting. Presentation logic, so it lives with the screen, not the ViewModel. */
private fun greetingRes(): Int = when (LocalTime.now().hour) {
    in 5..11 -> R.string.dashboard_greeting_morning
    in 12..17 -> R.string.dashboard_greeting_afternoon
    in 18..21 -> R.string.dashboard_greeting_evening
    else -> R.string.dashboard_greeting_generic
}

private data class QuickAction(
    val icon: ImageVector,
    val titleRes: Int,
    val subtitleRes: Int,
    val onClick: () -> Unit,
)

private fun quickActions(
    onOpenHistory: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenNotifications: () -> Unit,
): List<QuickAction> = listOf(
    QuickAction(
        icon = Icons.Filled.History,
        titleRes = R.string.dashboard_action_history_title,
        subtitleRes = R.string.dashboard_action_history_subtitle,
        onClick = onOpenHistory,
    ),
    QuickAction(
        icon = Icons.Filled.Insights,
        titleRes = R.string.dashboard_action_progress_title,
        subtitleRes = R.string.dashboard_action_progress_subtitle,
        onClick = onOpenProgress,
    ),
    QuickAction(
        icon = Icons.Filled.Notifications,
        titleRes = R.string.dashboard_action_notifications_title,
        subtitleRes = R.string.dashboard_action_notifications_subtitle,
        onClick = onOpenNotifications,
    ),
)

@Preview(name = "Dashboard — get started")
@Composable
private fun DashboardPreview() {
    PerioComplianceTheme {
        Surface {
            HomeDashboardContent(
                state = HomeDashboardUiState(
                    isLoading = false,
                    summary = DashboardSummary(
                        user = DashboardUser(
                            fullName = "Jane Smith",
                            firstName = "Jane",
                            email = "jane.smith@practice.com",
                            isEmailVerified = true,
                        ),
                        progress = ProgressSnapshot.Empty,
                    ),
                ),
                onRefresh = {},
                onStartScan = {},
                onOpenHistory = {},
                onOpenProgress = {},
                onOpenNotifications = {},
                onSignOut = {},
            )
        }
    }
}
