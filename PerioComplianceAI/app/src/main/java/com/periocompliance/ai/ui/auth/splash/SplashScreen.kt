package com.periocompliance.ai.ui.auth.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periocompliance.ai.R
import com.periocompliance.ai.ui.theme.PerioComplianceTheme
import com.periocompliance.ai.ui.theme.PerioTheme

@Composable
fun SplashScreen(
    onDecided: (StartDestination) -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        if (destination != StartDestination.Undecided) {
            onDecided(destination)
        }
    }

    SplashContent()
}

@Composable
private fun SplashContent() {
    // A short fade/scale in. The splash is not on screen long enough for anything more elaborate,
    // and a logo that is still animating when the screen changes looks broken rather than polished.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        label = "splashAlpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        label = "splashScale",
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.MedicalServices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(72.dp)
                    .alpha(alpha)
                    .scale(scale),
            )
            Spacer(Modifier.height(PerioTheme.spacing.md))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(alpha),
            )
        }
    }
}

@Preview
@Composable
private fun SplashPreview() {
    PerioComplianceTheme { SplashContent() }
}
