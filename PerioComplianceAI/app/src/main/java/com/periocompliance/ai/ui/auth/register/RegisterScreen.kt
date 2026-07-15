package com.periocompliance.ai.ui.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periocompliance.ai.R
import com.periocompliance.ai.ui.auth.components.AuthErrorBanner
import com.periocompliance.ai.ui.auth.components.ButtonSpinner
import com.periocompliance.ai.ui.auth.components.PasswordStrengthMeter
import com.periocompliance.ai.ui.auth.components.PerioPasswordField
import com.periocompliance.ai.ui.auth.components.PerioTextField
import com.periocompliance.ai.ui.theme.PerioComplianceTheme
import com.periocompliance.ai.ui.theme.PerioTheme

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.status) {
        if (state.status == RegisterStatus.Registered) {
            viewModel.onNavigationHandled()
            onRegistered()
        }
    }

    RegisterContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmChange = viewModel::onConfirmPasswordChange,
        onTogglePassword = viewModel::onTogglePasswordVisibility,
        onToggleConfirm = viewModel::onToggleConfirmVisibility,
        onSubmit = viewModel::onSubmit,
        onBack = onBack,
        onSignIn = onSignIn,
    )
}

@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onToggleConfirm: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
) {
    val spacing = PerioTheme.spacing
    val loading = state.status == RegisterStatus.Loading

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
        ) {
            IconButton(onClick = onBack, enabled = !loading) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                )
            }

            Spacer(Modifier.height(spacing.md))

            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(spacing.lg))

            AuthErrorBanner(
                error = (state.status as? RegisterStatus.Failed)?.error,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(spacing.md))

            PerioTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.register_name_label),
                placeholder = stringResource(R.string.register_name_hint),
                leadingIcon = Icons.Filled.Person,
                error = state.nameError,
                enabled = !loading,
            )

            Spacer(Modifier.height(spacing.md))

            PerioTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = stringResource(R.string.login_email_label),
                placeholder = stringResource(R.string.register_email_hint),
                leadingIcon = Icons.Filled.MailOutline,
                error = state.emailError,
                enabled = !loading,
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
                enabled = !loading,
            )

            PasswordStrengthMeter(strength = state.strength)

            Spacer(Modifier.height(spacing.md))

            PerioPasswordField(
                value = state.confirmPassword,
                onValueChange = onConfirmChange,
                label = stringResource(R.string.register_confirm_label),
                isVisible = state.isConfirmVisible,
                onToggleVisibility = onToggleConfirm,
                leadingIcon = Icons.Filled.Lock,
                error = state.confirmError,
                enabled = !loading,
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
                if (loading) ButtonSpinner() else Text(stringResource(R.string.register_submit))
            }

            Spacer(Modifier.height(spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.register_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onSignIn, enabled = !loading) {
                    Text(
                        text = stringResource(R.string.register_sign_in),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(Modifier.height(spacing.xl))
        }
    }
}

@Preview(name = "Register")
@Composable
private fun RegisterPreview() {
    PerioComplianceTheme {
        RegisterContent(
            state = RegisterUiState(
                name = "Dr. Jane Smith",
                email = "jane.smith@practice.com",
                password = "Str0ng!Passw0rd",
                confirmPassword = "Str0ng!Passw0rd",
                strength = com.periocompliance.ai.domain.validation.PasswordStrength.Strong,
            ),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmChange = {},
            onTogglePassword = {},
            onToggleConfirm = {},
            onSubmit = {},
            onBack = {},
            onSignIn = {},
        )
    }
}
