package com.periocompliance.ai.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.usecase.RegisterUseCase
import com.periocompliance.ai.domain.validation.FieldError
import com.periocompliance.ai.domain.validation.PasswordStrength
import com.periocompliance.ai.domain.validation.passwordStrength
import com.periocompliance.ai.domain.validation.validateConfirmPassword
import com.periocompliance.ai.domain.validation.validateEmail
import com.periocompliance.ai.domain.validation.validateName
import com.periocompliance.ai.domain.validation.validatePassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RegisterStatus {
    data object Idle : RegisterStatus
    data object Loading : RegisterStatus
    /** Account created and the verification email is away. The screen moves to the gate. */
    data object Registered : RegisterStatus
    data class Failed(val error: AuthError) : RegisterStatus
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: FieldError? = null,
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val confirmError: FieldError? = null,
    val strength: PasswordStrength = PasswordStrength.None,
    val isPasswordVisible: Boolean = false,
    val isConfirmVisible: Boolean = false,
    val status: RegisterStatus = RegisterStatus.Idle,
) {
    /**
     * The spec's "no submit until valid". Every field must be filled AND clean -- checking only
     * that the errors are null would let an untouched form through, since a field nobody has typed
     * in has no error yet.
     */
    val canSubmit: Boolean
        get() = name.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            nameError == null &&
            emailError == null &&
            passwordError == null &&
            confirmError == null &&
            status != RegisterStatus.Loading
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val register: RegisterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update {
            it.copy(
                name = value,
                nameError = if (value.isBlank()) null else validateName(value),
                status = it.status.clearedOnEdit(),
            )
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = if (value.isBlank()) null else validateEmail(value),
                status = it.status.clearedOnEdit(),
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { state ->
            state.copy(
                password = value,
                passwordError = if (value.isBlank()) null else validatePassword(value),
                strength = passwordStrength(value),
                // Re-check the confirmation against the new password. Without this, changing the
                // password after confirming it leaves a stale "matches" and lets a mismatched pair
                // through.
                confirmError = if (state.confirmPassword.isBlank()) {
                    null
                } else {
                    validateConfirmPassword(value, state.confirmPassword)
                },
                status = state.status.clearedOnEdit(),
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { state ->
            state.copy(
                confirmPassword = value,
                confirmError = if (value.isBlank()) {
                    null
                } else {
                    validateConfirmPassword(state.password, value)
                },
                status = state.status.clearedOnEdit(),
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleConfirmVisibility() {
        _uiState.update { it.copy(isConfirmVisible = !it.isConfirmVisible) }
    }

    fun onSubmit() {
        val state = _uiState.value

        // Re-validate everything on submit, not just what was touched. The screen disables the
        // button, but the ViewModel cannot assume the screen is the only caller.
        val nameError = validateName(state.name)
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        val confirmError = validateConfirmPassword(state.password, state.confirmPassword)

        if (nameError != null || emailError != null || passwordError != null || confirmError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmError = confirmError,
                )
            }
            return
        }

        _uiState.update { it.copy(status = RegisterStatus.Loading) }

        viewModelScope.launch {
            val result = register(state.name, state.email, state.password)
            _uiState.update {
                it.copy(
                    status = when (result) {
                        is AuthResult.Success -> RegisterStatus.Registered
                        is AuthResult.Failure -> RegisterStatus.Failed(result.error)
                    },
                )
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(status = RegisterStatus.Idle) }
    }
}

private fun RegisterStatus.clearedOnEdit(): RegisterStatus =
    if (this is RegisterStatus.Failed) RegisterStatus.Idle else this
