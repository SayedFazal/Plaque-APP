package com.periocompliance.ai.ui.scanresult

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periocompliance.ai.R
import com.periocompliance.ai.domain.model.ScanResult
import com.periocompliance.ai.ui.auth.components.messageRes
import com.periocompliance.ai.ui.theme.PerioTheme

/** Module 5 — AI analysis results for a scan. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    scanId: String,
    onClose: () -> Unit,
    viewModel: ScanResultViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(scanId) {
        viewModel.loadOrAnalyze(scanId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.result_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.result_cd_close))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
            when {
                state.isLoading -> LoadingState()

                state.result != null -> ResultContent(
                    result = state.result!!,
                    scanId = scanId,
                    onRetry = { viewModel.onRetry(scanId) },
                    onClose = onClose,
                )

                state.error != null -> ErrorState(
                    errorMessageRes = state.error!!.messageRes(),
                    onRetry = { viewModel.onRetry(scanId) },
                    onDismiss = viewModel::onErrorDismissed,
                )

                else -> ErrorState(
                    errorMessageRes = R.string.result_unknown_error,
                    onRetry = { viewModel.onRetry(scanId) },
                    onDismiss = viewModel::onErrorDismissed,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PerioTheme.spacing.md),
        ) {
            CircularProgressIndicator()
            Text(
                stringResource(R.string.result_analyzing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResultContent(
    result: ScanResult,
    scanId: String,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(PerioTheme.spacing.screenMargin),
        verticalArrangement = Arrangement.spacedBy(PerioTheme.spacing.md),
    ) {
        item {
            Text(
                stringResource(R.string.result_metrics),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        item {
            MetricCard(
                label = stringResource(R.string.result_bleeding),
                percentage = result.bleeding,
            )
        }

        item {
            MetricCard(
                label = stringResource(R.string.result_inflammation),
                percentage = result.inflammation,
            )
        }

        item {
            MetricCard(
                label = stringResource(R.string.result_plaque),
                percentage = result.plaque,
            )
        }

        item {
            Spacer(Modifier.height(PerioTheme.spacing.lg))
            OverallScoreCard(score = result.overall_score)
        }

        item {
            Spacer(Modifier.height(PerioTheme.spacing.lg))
            Text(
                stringResource(R.string.result_recommendations),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        items(result.recommendations.size) { index ->
            RecommendationItem(text = result.recommendations[index])
        }

        item {
            Spacer(Modifier.height(PerioTheme.spacing.lg))
            Button(
                onClick = onClose,
                shape = PerioTheme.shapes.button,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PerioTheme.spacing.minTouchTarget),
            ) {
                Text(stringResource(R.string.result_done))
            }
        }

        item {
            Text(
                stringResource(R.string.result_provider, result.provider, result.modelVersion),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = PerioTheme.spacing.sm),
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, percentage: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = PerioTheme.shapes.card,
            )
            .padding(PerioTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(PerioTheme.spacing.sm))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = when {
                percentage < 30 -> PerioTheme.colors.success
                percentage < 60 -> PerioTheme.colors.warning
                else -> MaterialTheme.colorScheme.error
            },
        )
    }
}

@Composable
private fun OverallScoreCard(score: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = PerioTheme.shapes.card,
            )
            .padding(PerioTheme.spacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PerioTheme.spacing.sm),
        ) {
            Text(
                stringResource(R.string.result_overall),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "$score%",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun RecommendationItem(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = PerioTheme.shapes.card,
            )
            .padding(PerioTheme.spacing.md),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Composable
private fun ErrorState(
    errorMessageRes: Int,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PerioTheme.spacing.screenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(R.string.result_error),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PerioTheme.spacing.sm))
        Text(
            stringResource(errorMessageRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PerioTheme.spacing.lg))
        Button(
            onClick = onRetry,
            shape = PerioTheme.shapes.button,
            modifier = Modifier
                .fillMaxWidth()
                .height(PerioTheme.spacing.minTouchTarget),
        ) {
            Text(stringResource(R.string.result_retry))
        }
        Spacer(Modifier.height(PerioTheme.spacing.sm))
        OutlinedButton(
            onClick = onDismiss,
            shape = PerioTheme.shapes.button,
            modifier = Modifier
                .fillMaxWidth()
                .height(PerioTheme.spacing.minTouchTarget),
        ) {
            Text(stringResource(R.string.result_dismiss))
        }
    }
}
