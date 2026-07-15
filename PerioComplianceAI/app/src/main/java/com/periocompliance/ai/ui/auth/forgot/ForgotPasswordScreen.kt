package com.periocompliance.ai.ui.auth.forgot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periocompliance.ai.R
import com.periocompliance.ai.ui.auth.components.AuthErrorBanner
import com.periocompliance.ai.ui.auth.components.ButtonSpinner
import com.periocompliance.ai.ui.auth.components.PerioTextField
import com.periocompliance.ai.ui.theme.PerioComplianceTheme
import com.periocompliance.ai.ui.theme.PerioTheme

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ForgotPasswordContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onSubmit = viewModel::onSubmit,
        onTryAgain = viewModel::onTryAgain,
        onBack = onBack,
    )
}

@Composable
private fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onTryAgain: () -> Unit,
    onBack: () -> Unit,
) {
    val spacing = PerioTheme.spacing
    val loading = state.status == ForgotStatus.Loading

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = spacing.screenMargin),
        ) {
            IconButton(onClick = onBack, enabled = !loading) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                )
            }

            Spacer(Modifier.height(spacing.lg))

            if (state.status == ForgotStatus.Sent) {
                SentConfirmation(email = state.email, onTryAgain = onTryAgain, onBack = onBack)
            } else {
                RequestForm(
                    state = state,
                    loading = loading,
                    onEmailChange = onEmailChange,
                    onSubmit = onSubmit,
                )
            }
        }
    }
}

@Composable
private fun RequestForm(
    state: ForgotPasswordUiState,
    loading: Boolean,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val spacing = PerioTheme.spacing

    Text(
        text = stringResource(R.string.forgot_title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(Modifier.height(spacing.lg))

    AuthErrorBanner(
        error = (state.status as? ForgotStatus.Failed)?.error,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(spacing.md))

    PerioTextField(
        value = state.email,
        onValueChange = onEmailChange,
        label = stringResource(R.string.forgot_email_label),
        placeholder = stringResource(R.string.login_email_hint),
        leadingIcon = Icons.Filled.MailOutline,
        error = state.emailError,
        enabled = !loading,
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Done,
    )

    Spacer(Modifier.height(spacing.lg))

    Button(
        onClick = onSubmit,
        enabled = state.canSubmit,
        shape = PerioTheme.shapes.button,
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.minTouchTarget),
    ) {
        if (loading) ButtonSpinner() else Text(stringResource(R.string.forgot_submit))
    }

    Spacer(Modifier.height(spacing.lg))

    Text(
        text = stringResource(R.string.forgot_security_note),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

/**
 * Shown for any address the user types, whether or not it has an account. That is deliberate --
 * see AuthRepositoryImpl.sendPasswordReset. The copy says "if an account exists" so the screen is
 * not lying to the user, it is simply not confirming anything to an attacker.
 */
@Composable
private fun SentConfirmation(
    email: String,
    onTryAgain: () -> Unit,
    onBack: () -> Unit,
) {
    val spacing = PerioTheme.spacing

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(spacing.xl))

        Icon(
            imageVector = Icons.Filled.MarkEmailRead,
            contentDescription = null,
            tint = PerioTheme.colors.success,
            modifier = Modifier.size(64.dp),
        )

        Spacer(Modifier.height(spacing.lg))

        Text(
            text = stringResource(R.string.forgot_sent_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(spacing.sm))

        Text(
            text = stringResource(R.string.forgot_sent_body, email),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(spacing.xl))

        Button(
            onClick = onBack,
            shape = PerioTheme.shapes.button,
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.minTouchTarget),
        ) {
            Text(stringResource(R.string.forgot_back))
        }

        TextButton(onClick = onTryAgain) {
            Text(stringResource(R.string.forgot_resend))
        }
    }
}

@Preview(name = "Forgot password")
@Composable
private fun ForgotPreview() {
    PerioComplianceTheme {
        ForgotPasswordContent(
            state = ForgotPasswordUiState(email = "dr.smith@clinic.com"),
            onEmailChange = {},
            onSubmit = {},
            onTryAgain = {},
            onBack = {},
        )
    }
}

@Preview(name = "Forgot password - sent")
@Composable
private fun ForgotSentPreview() {
    PerioComplianceTheme {
        ForgotPasswordContent(
            state = ForgotPasswordUiState(
                email = "dr.smith@clinic.com",
                status = ForgotStatus.Sent,
            ),
            onEmailChange = {},
            onSubmit = {},
            onTryAgain = {},
            onBack = {},
        )
    }
}
