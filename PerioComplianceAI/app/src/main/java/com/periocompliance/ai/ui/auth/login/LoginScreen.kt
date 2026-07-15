package com.periocompliance.ai.ui.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periocompliance.ai.R
import com.periocompliance.ai.ui.auth.components.AuthErrorBanner
import com.periocompliance.ai.ui.auth.components.ButtonSpinner
import com.periocompliance.ai.ui.auth.components.PerioPasswordField
import com.periocompliance.ai.ui.auth.components.PerioTextField
import com.periocompliance.ai.ui.theme.PerioComplianceTheme
import com.periocompliance.ai.ui.theme.PerioTheme

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNeedsVerification: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigation is a side effect of state, not of the click. If it were done in onClick, a
    // configuration change mid-request would drop it on the floor.
    LaunchedEffect(state.status) {
        when (state.status) {
            LoginStatus.Success -> {
                viewModel.onNavigationHandled()
                onLoggedIn()
            }

            LoginStatus.NeedsVerification -> {
                viewModel.onNavigationHandled()
                onNeedsVerification()
            }

            else -> Unit
        }
    }

    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onRememberMeChange = viewModel::onRememberMeChange,
        onTogglePassword = viewModel::onTogglePasswordVisibility,
        onSubmit = viewModel::onSubmit,
        onForgotPassword = onForgotPassword,
        onRegister = onRegister,
    )
}

/**
 * Stateless, so it can be previewed and tested without Hilt or Firebase. Every auth screen in this
 * module follows the same split.
 */
@Composable
private fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onTogglePassword: () -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val spacing = PerioTheme.spacing

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(spacing.xxl))

            Icon(
                imageVector = Icons.Filled.MedicalServices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(spacing.md))

            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(spacing.xl))

            AuthErrorBanner(
                error = (state.status as? LoginStatus.Failed)?.error,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(spacing.md))

            PerioTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = stringResource(R.string.login_email_label),
                placeholder = stringResource(R.string.login_email_hint),
                leadingIcon = Icons.Filled.MailOutline,
                error = state.emailError,
                enabled = state.status != LoginStatus.Loading,
                keyboardType = KeyboardType.Email,
            )

            Spacer(Modifier.height(spacing.md))

            PerioPasswordField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.login_password_label),
                isVisible = state.isPasswordVisible,
                onToggleVisibility = onTogglePassword,
                leadingIcon = Icons.Filled.Lock,
                error = state.passwordError,
                enabled = state.status != LoginStatus.Loading,
                imeAction = ImeAction.Done,
            )

            Spacer(Modifier.height(spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.rememberMe,
                        onCheckedChange = onRememberMeChange,
                        enabled = state.status != LoginStatus.Loading,
                    )
                    Text(
                        text = stringResource(R.string.login_remember),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onForgotPassword) {
                    Text(
                        text = stringResource(R.string.login_forgot),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(Modifier.height(spacing.md))

            Button(
                onClick = onSubmit,
                enabled = state.canSubmit,
                shape = PerioTheme.shapes.button,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.minTouchTarget),
            ) {
                if (state.status == LoginStatus.Loading) {
                    ButtonSpinner()
                } else {
                    Text(stringResource(R.string.login_submit))
                }
            }

            Spacer(Modifier.height(spacing.lg))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onRegister) {
                    Text(
                        text = stringResource(R.string.login_register),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(Modifier.height(spacing.xl))
        }
    }
}

@Preview(name = "Login")
@Composable
private fun LoginPreview() {
    PerioComplianceTheme {
        LoginContent(
            state = LoginUiState(email = "dr.smith@clinic.com", password = "secret123"),
            onEmailChange = {},
            onPasswordChange = {},
            onRememberMeChange = {},
            onTogglePassword = {},
            onSubmit = {},
            onForgotPassword = {},
            onRegister = {},
        )
    }
}

@Preview(name = "Login - error, dark")
@Composable
private fun LoginErrorPreview() {
    PerioComplianceTheme(darkTheme = true) {
        LoginContent(
            state = LoginUiState(
                email = "dr.smith@clinic.com",
                password = "wrong",
                status = LoginStatus.Failed(com.periocompliance.ai.domain.model.AuthError.InvalidCredentials),
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onRememberMeChange = {},
            onTogglePassword = {},
            onSubmit = {},
            onForgotPassword = {},
            onRegister = {},
        )
    }
}
