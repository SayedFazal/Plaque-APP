package com.periocompliance.ai.ui.scanresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.ScanResult
import com.periocompliance.ai.domain.usecase.AnalyzeScanUseCase
import com.periocompliance.ai.domain.usecase.GetScanResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanResultUiState(
    val isLoading: Boolean = false,
    val result: ScanResult? = null,
    val error: AuthError? = null,
)

/**
 * Drives the scan result screen (Module 5).
 *
 * Triggers AI analysis on a given scan and displays the results (metrics, recommendations).
 * The analysis is idempotent: re-triggering returns the existing result without re-running
 * the AI. Results are persisted in the database and survive app restarts.
 */
@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val analyzeScan: AnalyzeScanUseCase,
    private val getResult: GetScanResultUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanResultUiState())
    val uiState: StateFlow<ScanResultUiState> = _uiState.asStateFlow()

    fun loadOrAnalyze(scanId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // Try to fetch existing result first; if none, analyze.
            when (val result = getResult(scanId)) {
                is AuthResult.Success -> {
                    if (result.data != null) {
                        _uiState.update { it.copy(isLoading = false, result = result.data) }
                    } else {
                        // No existing result; trigger analysis.
                        analyze(scanId)
                    }
                }
                is AuthResult.Failure -> {
                    // Error fetching; try analyzing instead.
                    analyze(scanId)
                }
            }
        }
    }

    private suspend fun analyze(scanId: String) {
        when (val result = analyzeScan(scanId)) {
            is AuthResult.Success -> {
                _uiState.update { it.copy(isLoading = false, result = result.data, error = null) }
            }
            is AuthResult.Failure -> {
                _uiState.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    fun onRetry(scanId: String) {
        loadOrAnalyze(scanId)
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
    }
}
