package com.periocompliance.ai.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.ScanSummary
import com.periocompliance.ai.domain.usecase.GetScanSummaryUseCase
import com.periocompliance.ai.domain.usecase.SubmitDailyScanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyScanUiState(
    /** Initial "have they already scanned today?" check, before the camera is shown. */
    val checking: Boolean = true,
    val alreadyScannedToday: Boolean = false,
    val submitted: Boolean = false,
    val isSubmitting: Boolean = false,
    val summary: ScanSummary? = null,
    val error: AuthError? = null,
) {
    /** Show the completion screen instead of the camera when today's scan is already on record. */
    val isComplete: Boolean get() = submitted || alreadyScannedToday
}

/**
 * Drives the daily scan (Module 3). The camera preview and capture are the screen's concern; this
 * owns the two things that touch the backend: an entry check for "already scanned today" (so a user
 * who is done sees their streak instead of being asked to scan again) and the submit itself.
 *
 * The submit records the compliance event — recording that today's scan happened — which is what the
 * dashboard's streak and count are built from. Uploading the captured image is Module 4.
 */
@HiltViewModel
class DailyScanViewModel @Inject constructor(
    private val submitDailyScan: SubmitDailyScanUseCase,
    private val getScanSummary: GetScanSummaryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyScanUiState())
    val uiState: StateFlow<DailyScanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getScanSummary()) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(
                        checking = false,
                        summary = result.data,
                        alreadyScannedToday = result.data.scannedToday,
                    )
                }
                // A failed status check must not block capture — the submit will surface any real
                // error. Just fall through to the camera.
                is AuthResult.Failure -> _uiState.update { it.copy(checking = false) }
            }
        }
    }

    fun onSubmit() {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            when (val result = submitDailyScan()) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitted = true,
                        summary = result.data,
                        error = null,
                    )
                }
                is AuthResult.Failure -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.error)
                }
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
    }
}
