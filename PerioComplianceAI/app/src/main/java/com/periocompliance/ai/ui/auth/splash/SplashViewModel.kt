package com.periocompliance.ai.ui.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periocompliance.ai.domain.repository.AuthRepository
import com.periocompliance.ai.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Where the app opens. Decided once, on launch. */
sealed interface StartDestination {
    data object Undecided : StartDestination
    data object Onboarding : StartDestination
    data object Login : StartDestination
    data object VerifyEmail : StartDestination
    data object Home : StartDestination
}

/**
 * Session persistence lives here.
 *
 * Firebase restores the signed-in user from disk before this runs, so "keep the user logged in
 * after restart" needs no token handling of our own -- currentUser() is either there or it is not.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<StartDestination>(StartDestination.Undecided)
    val destination: StateFlow<StartDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val seenOnboarding = sessionRepository.hasSeenOnboarding.first()
            val user = authRepository.currentUser()

            _destination.value = when {
                user == null && !seenOnboarding -> StartDestination.Onboarding
                user == null -> StartDestination.Login
                // A signed-in but unverified user is not allowed past the gate, even on a warm
                // start. Without this, killing the app during verification walks straight into the
                // dashboard.
                !user.isEmailVerified -> StartDestination.VerifyEmail
                else -> StartDestination.Home
            }
        }
    }
}
