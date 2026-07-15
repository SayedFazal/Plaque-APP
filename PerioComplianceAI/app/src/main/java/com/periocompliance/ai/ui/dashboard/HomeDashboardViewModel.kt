package com.periocompliance.ai.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.DashboardSummary
import com.periocompliance.ai.domain.usecase.ObserveDashboardUseCase
import com.periocompliance.ai.domain.usecase.RefreshDashboardUseCase
import com.periocompliance.ai.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeDashboardUiState(
    /** First paint with nothing cached yet — the only time a full-screen spinner shows. */
    val isLoading: Boolean = true,
    /** A refresh while content is already on screen — a quiet top-bar indicator, not a blank page. */
    val isRefreshing: Boolean = false,
    val summary: DashboardSummary? = null,
    /** Only set when there is nothing to show; a failed refresh over good data stays silent. */
    val error: AuthError? = null,
)

/**
 * Drives the patient home dashboard.
 *
 * Two data paths, on purpose: [ObserveDashboardUseCase] renders the persisted session instantly
 * (so a warm start never shows a spinner over data we already have), while [RefreshDashboardUseCase]
 * pulls a fresh profile from the backend. A refresh that fails when we already have cached content
 * is swallowed rather than blanking the screen — the dashboard is a read surface, and stale-but-real
 * beats an error page.
 */
@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    observeDashboard: ObserveDashboardUseCase,
    private val refreshDashboard: RefreshDashboardUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboard()
            .onEach { summary ->
                _uiState.update { state ->
                    state.copy(
                        summary = summary ?: state.summary,
                        // Once anything is on screen we are no longer in first-load.
                        isLoading = state.isLoading && summary == null,
                    )
                }
            }
            .launchIn(viewModelScope)

        refresh()
    }

    fun refresh() {
        val hasContent = _uiState.value.summary != null
        _uiState.update {
            it.copy(isLoading = !hasContent, isRefreshing = hasContent, error = null)
        }

        viewModelScope.launch {
            when (val result = refreshDashboard()) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(
                        summary = result.data,
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                    )
                }

                is AuthResult.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = if (it.summary == null) result.error else null,
                    )
                }
            }
        }
    }

    /**
     * Clears the session. Navigation away is the screen's job (see [SignOutUseCase] usage in the
     * auth module) — this only tears down the session, mirroring how VerifyEmail signs out.
     */
    fun onSignOut() {
        viewModelScope.launch { signOutUseCase() }
    }
}
