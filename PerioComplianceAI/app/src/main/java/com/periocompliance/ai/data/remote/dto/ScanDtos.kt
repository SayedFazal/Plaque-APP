package com.periocompliance.ai.data.remote.dto

import com.periocompliance.ai.domain.model.ScanSummary
import kotlinx.serialization.Serializable

/**
 * Wire format for /scans. Mirrors the backend JSON exactly and is mapped to the domain model in the
 * repository, so a change to the API shape stops at the data layer.
 */

@Serializable
data class SubmitScanRequest(val localDate: String)

@Serializable
data class ScanSummaryResponse(
    val scannedToday: Boolean,
    val streakDays: Int,
    val scansCompleted: Int,
    val complianceScore: Int? = null,
    val lastScanDate: String? = null,
) {
    fun toDomain() = ScanSummary(
        scannedToday = scannedToday,
        streakDays = streakDays,
        scansCompleted = scansCompleted,
        complianceScore = complianceScore,
        lastScanDate = lastScanDate,
    )
}
