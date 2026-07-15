package com.periocompliance.ai.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.usecase.SignInUseCase
import com.periocompliance.ai.domain.validation.FieldError
import com.periocompliance.ai.domain.validation.validateEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * What the login screen can be doing. Modelled as a sealed type rather than a pile of booleans, so
 * "loading and errored at the same time" is not representable.
 */
sealed interface LoginStatus {
    data object Idle : LoginStatus
    data object Loading : LoginStatus
    data object Success : LoginStatus
    /** Signed in, but the address is unconfirmed. The screen routes to the verification gate. */
    data object NeedsVerification : LoginStatus
    data class Failed(val error: AuthError) : LoginStatus
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val rememberMe: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val status: LoginStatus = LoginStatus.Idle,
) {
    /**
     * Login validates only that the fields are non-empty and the email is well-formed. It does NOT
     * apply the password complexity rules -- those belong to registration. Running them here would
     * lock an existing user out of their own account because a rule changed after they signed up.
     */
    val canSubmit: Boolean
        get() = email.isNotBlank() &&
            password.isNotBlank() &&
            emailError == null &&
            status != LoginStatus.Loading
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signIn: SignInUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                // Validate as they type, but only complain once they have typed something. Showing
                // "required" on an empty field the user has not reached yet is nagging, not help.
                emailError = if (value.isBlank()) null else validateEmail(value),
                status = it.status.clearedOnEdit(),
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                status = it.status.clearedOnEdit(),
            )
        }
    }

    fun onRememberMeChange(enabled: Boolean) {
        _uiState.update { it.copy(rememberMe = enabled) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onSubmit() {
        val state = _uiState.value
        val emailError = validateEmail(state.email)
        val passwordError = if (state.password.isEmpty()) FieldError.Required else null

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(status = LoginStatus.Loading) }

        viewModelScope.launch {
            val result = signIn(state.email, state.password, state.rememberMe)
            _uiState.update {
                it.copy(
                    status = when (result) {
                        is AuthResult.Success -> LoginStatus.Success
                        is AuthResult.Failure -> when (result.error) {
                            AuthError.EmailNotVerified -> LoginStatus.NeedsVerification
                            else -> LoginStatus.Failed(result.error)
                        }
                    },
                )
            }
        }
    }

    /** Consumed by the screen after it has navigated, so returning back does not re-fire. */
    fun onNavigationHandled() {
        _uiState.update { it.copy(status = LoginStatus.Idle) }
    }
}

/** Editing a field dismisses the previous failure banner -- the user is already fixing it. */
private fun LoginStatus.clearedOnEdit(): LoginStatus =
    if (this is LoginStatus.Failed) LoginStatus.Idle else this
