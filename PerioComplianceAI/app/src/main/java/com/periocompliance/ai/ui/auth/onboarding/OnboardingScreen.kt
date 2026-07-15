package com.periocompliance.ai.ui.auth.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.periocompliance.ai.R
import com.periocompliance.ai.ui.theme.PerioComplianceTheme
import com.periocompliance.ai.ui.theme.PerioTheme
import kotlinx.coroutines.launch

/** The three exported onboarding screens: track, ai_analysis, compliance. */
private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Filled.MonitorHeart,
        title = "Track your gum health",
        body = "A daily scan builds a record your dentist can actually act on.",
    ),
    OnboardingPage(
        icon = Icons.Filled.AutoAwesome,
        title = "AI-driven precision",
        body = "Elevating compliance through intelligent analysis of every scan.",
    ),
    OnboardingPage(
        icon = Icons.Filled.VerifiedUser,
        title = "Stay compliant",
        body = "Streaks, reminders and progress you can show at your next appointment.",
    ),
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    OnboardingContent(onFinished = { viewModel.onFinished(onFinished) })
}

@Composable
private fun OnboardingContent(onFinished: () -> Unit) {
    val spacing = PerioTheme.spacing
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = spacing.screenMargin),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                // Skip is not a lesser choice -- it still marks onboarding as seen, so the
                // carousel does not reappear on the next launch.
                TextButton(onClick = onFinished) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(96.dp),
                    )
                    Spacer(Modifier.height(spacing.xl))
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(spacing.sm))
                    Text(
                        text = page.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            PageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.lg),
            )

            Button(
                onClick = {
                    if (isLastPage) {
                        onFinished()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                shape = PerioTheme.shapes.button,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.minTouchTarget),
            ) {
                Text(
                    stringResource(
                        if (isLastPage) R.string.onboarding_start else R.string.onboarding_next,
                    ),
                )
            }

            Spacer(Modifier.height(spacing.xl))
        }
    }
}

/** The active dot stretches into a pill rather than just changing colour. */
@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(PerioTheme.spacing.sm, Alignment.CenterHorizontally),
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            val width by animateDpAsState(if (active) 24.dp else 8.dp, label = "dotWidth")
            val color by animateColorAsState(
                if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                label = "dotColor",
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .background(color, PerioTheme.shapes.chip),
            )
        }
    }
}

@Preview
@Composable
private fun OnboardingPreview() {
    PerioComplianceTheme { OnboardingContent(onFinished = {}) }
}
