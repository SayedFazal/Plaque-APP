package com.periocompliance.ai.ui.auth.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.repository.AuthRepository
import com.periocompliance.ai.domain.usecase.RefreshVerificationStatusUseCase
import com.periocompliance.ai.domain.usecase.SendVerificationEmailUseCase
import com.periocompliance.ai.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RESEND_COOLDOWN_SECONDS = 60

sealed interface VerifyStatus {
    data object Idle : VerifyStatus
    data object Checking : VerifyStatus
    /** The address is confirmed; the screen may open the main graph. */
    data object Verified : VerifyStatus
    /** Checked, still not verified. Not an error -- the user simply has not clicked the link yet. */
    data object StillUnverified : VerifyStatus
    data object EmailSent : VerifyStatus
    data class Failed(val error: AuthError) : VerifyStatus
}

data class VerifyEmailUiState(
    val email: String = "",
    val status: VerifyStatus = VerifyStatus.Idle,
    val resendCooldownSeconds: Int = 0,
) {
    val canResend: Boolean
        get() = resendCooldownSeconds == 0 && status != VerifyStatus.Checking
}

/**
 * The verification gate.
 *
 * The subtlety: FirebaseUser.isEmailVerified is read from the cached ID token, so a user who has
 * just clicked the link in their inbox is still "unverified" to this app until the user object is
 * reloaded from the server. That is what [RefreshVerificationStatusUseCase] is for, and it is why
 * this screen has an explicit "I've verified" button rather than trying to guess.
 */
@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val refreshVerificationStatus: RefreshVerificationStatusUseCase,
    private val sendVerificationEmail: SendVerificationEmailUseCase,
    private val signOutUseCase: SignOutUseCase,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VerifyEmailUiState(email = authRepository.currentUser()?.email.orEmpty()),
    )
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

    init {
        // Registration already sent one email, so the button starts on cooldown. Otherwise the
        // first thing a user sees is a live "Resend" that would send a duplicate.
        startCooldown()
    }

    fun onCheckVerification() {
        _uiState.update { it.copy(status = VerifyStatus.Checking) }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = when (val result = refreshVerificationStatus()) {
                        is AuthResult.Success ->
                            if (result.data.isEmailVerified) {
                                VerifyStatus.Verified
                            } else {
                                VerifyStatus.StillUnverified
                            }

                        is AuthResult.Failure -> VerifyStatus.Failed(result.error)
                    },
                )
            }
        }
    }

    fun onResend() {
        if (!_uiState.value.canResend) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = when (val result = sendVerificationEmail()) {
                        is AuthResult.Success -> VerifyStatus.EmailSent
                        is AuthResult.Failure -> VerifyStatus.Failed(result.error)
                    },
                )
            }
            startCooldown()
        }
    }

    fun onSignOut() {
        viewModelScope.launch { signOutUseCase() }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(status = VerifyStatus.Idle) }
    }

    private fun startCooldown() {
        viewModelScope.launch {
            for (remaining in RESEND_COOLDOWN_SECONDS downTo 0) {
                _uiState.update { it.copy(resendCooldownSeconds = remaining) }
                if (remaining > 0) delay(1_000)
            }
        }
    }
}
