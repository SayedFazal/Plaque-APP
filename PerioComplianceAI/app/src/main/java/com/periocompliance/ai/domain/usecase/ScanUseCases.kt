package com.periocompliance.ai.domain.usecase

import android.net.Uri
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.ScanSummary
import com.periocompliance.ai.domain.repository.ScanRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Module 3 & 4 use cases. They own the one rule the repository should not: what "today" means is
 * the device's local calendar date, resolved here so neither the screen nor the data layer formats dates.
 */

class SubmitDailyScanUseCase @Inject constructor(
    private val repository: ScanRepository,
) {
    suspend operator fun invoke(): AuthResult<ScanSummary> =
        repository.submitScan(LocalDate.now().toString())
}

class GetScanSummaryUseCase @Inject constructor(
    private val repository: ScanRepository,
) {
    suspend operator fun invoke(): AuthResult<ScanSummary> =
        repository.refreshSummary(LocalDate.now().toString())
}

class SubmitScanWithImageUseCase @Inject constructor(
    private val repository: ScanRepository,
) {
    suspend operator fun invoke(imageUri: Uri): AuthResult<ScanSummary> =
        repository.submitScanWithImage(LocalDate.now().toString(), imageUri)
}
