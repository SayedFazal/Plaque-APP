package com.periocompliance.ai.ui.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periocompliance.ai.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    /**
     * Marks onboarding done. Called on both "Get Started" and "Skip" -- a user who skipped has
     * still made a decision, and showing the carousel again on next launch would ignore it.
     */
    fun onFinished(then: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.setOnboardingSeen()
            then()
        }
    }
}
