package com.periocompliance.ai.domain.usecase

import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.ScanResult
import com.periocompliance.ai.domain.repository.ScanResultRepository
import javax.inject.Inject

/** Module 5 use cases for AI analysis results. */

class AnalyzeScanUseCase @Inject constructor(
    private val repository: ScanResultRepository,
) {
    suspend operator fun invoke(scanId: String): AuthResult<ScanResult> =
        repository.analyzeScan(scanId)
}

class GetScanResultUseCase @Inject constructor(
    private val repository: ScanResultRepository,
) {
    suspend operator fun invoke(scanId: String): AuthResult<ScanResult?> =
        repository.getResult(scanId)
}
