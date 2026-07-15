package com.periocompliance.ai.ui.auth.verify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periocompliance.ai.R
import com.periocompliance.ai.ui.auth.components.AuthErrorBanner
import com.periocompliance.ai.ui.auth.components.ButtonSpinner
import com.periocompliance.ai.ui.theme.PerioComplianceTheme
import com.periocompliance.ai.ui.theme.PerioTheme

/**
 * The gate. A registered but unverified user lands here and cannot get past it -- not by pressing
 * back, and not by killing and relaunching the app (SplashViewModel routes them straight back).
 */
@Composable
fun VerifyEmailScreen(
    onVerified: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: VerifyEmailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.status) {
        if (state.status == VerifyStatus.Verified) {
            viewModel.onNavigationHandled()
            onVerified()
        }
    }

    VerifyEmailContent(
        state = state,
        onCheck = viewModel::onCheckVerification,
        onResend = viewModel::onResend,
        onSignOut = {
            viewModel.onSignOut()
            onSignedOut()
        },
    )
}

@Composable
private fun VerifyEmailContent(
    state: VerifyEmailUiState,
    onCheck: () -> Unit,
    onResend: () -> Unit,
    onSignOut: () -> Unit,
) {
    val spacing = PerioTheme.spacing
    val checking = state.status == VerifyStatus.Checking

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = spacing.screenMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.MarkEmailRead,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp),
            )

            Spacer(Modifier.height(spacing.lg))

            Text(
                text = stringResource(R.string.verify_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(spacing.sm))

            Text(
                text = stringResource(R.string.verify_body, state.email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(spacing.lg))

            AuthErrorBanner(
                error = (state.status as? VerifyStatus.Failed)?.error,
                modifier = Modifier.fillMaxWidth(),
            )

            // "Still unverified" and "email sent" are not failures, so they get an informational
            // line rather than the error banner.
            when (state.status) {
                VerifyStatus.StillUnverified -> InfoLine(
                    text = stringResource(R.string.verify_still_unverified),
                    color = PerioTheme.colors.warning,
                )

                VerifyStatus.EmailSent -> InfoLine(
                    text = stringResource(R.string.verify_sent),
                    color = PerioTheme.colors.success,
                )

                else -> Unit
            }

            Spacer(Modifier.height(spacing.md))

            Button(
                onClick = onCheck,
                enabled = !checking,
                shape = PerioTheme.shapes.button,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.minTouchTarget),
            ) {
                if (checking) ButtonSpinner() else Text(stringResource(R.string.verify_check))
            }

            Spacer(Modifier.height(spacing.sm))

            OutlinedButton(
                onClick = onResend,
                enabled = state.canResend,
                shape = PerioTheme.shapes.button,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.minTouchTarget),
            ) {
                Text(
                    text = if (state.resendCooldownSeconds > 0) {
                        stringResource(R.string.verify_resend_in, state.resendCooldownSeconds)
                    } else {
                        stringResource(R.string.verify_resend)
                    },
                )
            }

            Spacer(Modifier.height(spacing.md))

            TextButton(onClick = onSignOut) {
                Text(stringResource(R.string.verify_sign_out))
            }
        }
    }
}

@Composable
private fun InfoLine(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PerioTheme.spacing.sm),
    )
}

@Preview(name = "Verify email")
@Composable
private fun VerifyPreview() {
    PerioComplianceTheme {
        VerifyEmailContent(
            state = VerifyEmailUiState(
                email = "jane.smith@practice.com",
                resendCooldownSeconds = 42,
            ),
            onCheck = {},
            onResend = {},
            onSignOut = {},
        )
    }
}
