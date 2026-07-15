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
 * The patient's compliance metrics.
 *
 * [complianceScore] is nullable rather than 0: a score of zero and "not enough history to compute a
 * score" are different states, and rendering "0%" for a brand-new account would be a lie.
 */
data class ProgressSnapshot(
    val streakDays: Int,
    val scansCompleted: Int,
    val complianceScore: Int?,
) {
    /** The single flag the dashboard switches on: stats row when true, get-started card when false. */
    val hasActivity: Boolean get() = scansCompleted > 0

    companion object {
        val Empty = ProgressSnapshot(streakDays = 0, scansCompleted = 0, complianceScore = null)
    }
}
