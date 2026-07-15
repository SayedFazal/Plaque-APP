package com.periocompliance.ai.domain.model

/**
 * The compliance numbers behind the daily scan (Module 3), provider-agnostic.
 *
 * Computed server-side from the set of days the user has scanned, so the client never has to do
 * streak or adherence math and two devices can never disagree. This is the same shape the dashboard's
 * [ProgressSnapshot] is built from — see the mapping in DashboardRepositoryImpl.
 */
data class ScanSummary(
    /** Whether a scan already exists for the user's local "today". */
    val scannedToday: Boolean,
    val streakDays: Int,
    val scansCompleted: Int,
    /** Trailing-window adherence percentage, or null until the first scan exists. */
    val complianceScore: Int?,
    /** Most recent scanned day as "YYYY-MM-DD", or null if never scanned. */
    val lastScanDate: String?,
)
