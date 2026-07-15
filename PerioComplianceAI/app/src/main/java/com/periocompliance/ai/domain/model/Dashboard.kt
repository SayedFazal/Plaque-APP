package com.periocompliance.ai.domain.model

/**
 * Everything the patient home dashboard (Module 2) renders, in provider-agnostic form.
 *
 * The identity fields come from the signed-in user (`/auth/me`). The [progress] metrics belong to
 * modules that do not exist yet — daily scanning (Module 3) is what first produces a streak, a scan
 * count and a compliance score. Until those land, [progress] is [ProgressSnapshot.Empty], and the
 * UI switches on [ProgressSnapshot.hasActivity] rather than special-casing "no backend yet". When
 * the scan endpoints ship, only the repository mapping changes; this model and every screen above
 * it stay put.
 */
data class DashboardSummary(
    val user: DashboardUser,
    val progress: ProgressSnapshot,
)

data class DashboardUser(
    val fullName: String,
    /** Just the first token of the name, for the greeting ("Good morning, Jane"). */
    val firstName: String,
    val email: String,
    val isEmailVerified: Boolean,
)

/**
 * The patient's compliance metrics (Module 3) and latest analysis results (Module 5).
 *
 * [complianceScore] is nullable rather than 0: a score of zero and "not enough history to compute a
 * score" are different states, and rendering "0%" for a brand-new account would be a lie.
 *
 * Analysis fields ([analysisScore], etc.) are present if the latest scan has been analyzed. The
 * dashboard fetches the latest ScanResult automatically; no extra user action needed. If analysis
 * does not exist, these fields are null and the UI shows the get-started state.
 */
data class ProgressSnapshot(
    val streakDays: Int,
    val scansCompleted: Int,
    val complianceScore: Int?,
    // Module 5: latest analysis results (null if latest scan is not yet analyzed)
    val analysisScore: Int? = null,
    val bleeding: Int? = null,
    val inflammation: Int? = null,
    val plaque: Int? = null,
) {
    /** The single flag the dashboard switches on: stats row when true, get-started card when false. */
    val hasActivity: Boolean get() = scansCompleted > 0

    /** True if the latest scan has analysis results available. */
    val hasAnalysis: Boolean get() = analysisScore != null

    companion object {
        val Empty = ProgressSnapshot(streakDays = 0, scansCompleted = 0, complianceScore = null)
    }
}
