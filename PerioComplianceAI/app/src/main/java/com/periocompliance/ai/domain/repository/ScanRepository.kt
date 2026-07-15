package com.periocompliance.ai.domain.repository

import android.net.Uri
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.ScanSummary
import kotlinx.coroutines.flow.StateFlow

/**
 * The door to daily-scan data (Modules 3 & 4).
 *
 * [summary] is a hot cache so the dashboard can react the instant a scan is recorded — submitting a
 * scan updates it, and anything observing (the dashboard's combined summary) updates with no extra
 * round trip. The suspend calls carry the client's local date so streaks are computed against the
 * user's own calendar, not the server's UTC clock.
 */
interface ScanRepository {

    /** Latest known summary, or null until first loaded. Updated by [submitScan] and [refreshSummary]. */
    val summary: StateFlow<ScanSummary?>

    /** Record today's scan (idempotent per day) and return the refreshed summary. Module 3. */
    suspend fun submitScan(localDate: String): AuthResult<ScanSummary>

    /** Re-fetch the summary for the given local date without recording a scan. */
    suspend fun refreshSummary(localDate: String): AuthResult<ScanSummary>

    /** Record today's scan with a captured image (Module 4). The image is compressed and uploaded. */
    suspend fun submitScanWithImage(localDate: String, imageUri: Uri): AuthResult<ScanSummary>
}
