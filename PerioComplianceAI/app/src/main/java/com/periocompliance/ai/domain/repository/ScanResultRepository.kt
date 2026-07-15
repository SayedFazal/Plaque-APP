package com.periocompliance.ai.domain.repository

import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.ScanResult
import kotlinx.coroutines.flow.StateFlow

/** Repository for scan analysis results (Module 5). */
interface ScanResultRepository {

    /** Latest known result for the current scan, or null until loaded. */
    val result: StateFlow<ScanResult?>

    /** Trigger AI analysis for a scan (idempotent; existing result is re-fetched). */
    suspend fun analyzeScan(scanId: String): AuthResult<ScanResult>

    /** Fetch the analysis result for a scan. */
    suspend fun getResult(scanId: String): AuthResult<ScanResult?>
}
