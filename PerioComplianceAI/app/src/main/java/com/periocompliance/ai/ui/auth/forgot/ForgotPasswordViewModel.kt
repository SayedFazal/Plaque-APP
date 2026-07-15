package com.periocompliance.ai.ui.auth.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.usecase.SendPasswordResetUseCase
import com.periocompliance.ai.domain.validation.FieldError
import com.periocompliance.ai.domain.validation.validateEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ForgotStatus {
    data object Idle : ForgotStatus
    data object Loading : ForgotStatus
    /**
     * Reached whether or not the address has an account. The repository deliberately swallows
     * "user not found" -- reporting it would let anyone check which dentists are registered.
     */
    data object Sent : ForgotStatus
    data class Failed(val error: AuthError) : ForgotStatus
}

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: FieldError? = null,
    val status: ForgotStatus = ForgotStatus.Idle,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && emailError == null && status != ForgotStatus.Loading
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordReset: SendPasswordResetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = if (value.isBlank()) null else validateEmail(value),
                status = if (it.status is ForgotStatus.Failed) ForgotStatus.Idle else it.status,
            )
        }
    }

    fun onSubmit() {
        val state = _uiState.value
        val emailError = validateEmail(state.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        _uiState.update { it.copy(status = ForgotStatus.Loading) }

        viewModelScope.launch {
            val result = sendPasswordReset(state.email)
            _uiState.update {
                it.copy(
                    status = when (result) {
                        is AuthResult.Success -> ForgotStatus.Sent
                        is AuthResult.Failure -> ForgotStatus.Failed(result.error)
                    },
                )
            }
        }
    }

    /** "Didn't receive the email? Try again" -- back to the form. */
    fun onTryAgain() {
        _uiState.update { it.copy(status = ForgotStatus.Idle) }
    }
}
