package com.periocompliance.ai.domain.repository

import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.DashboardSummary
import kotlinx.coroutines.flow.Flow

/**
 * The door to the home dashboard's data.
 *
 * Like [AuthRepository], everything above this interface is provider-agnostic: the screen and its
 * ViewModel never learn that today the summary is assembled from the auth session and an empty
 * progress snapshot, and that tomorrow it will be stitched from scan/compliance endpoints. That
 * swap happens entirely inside the data layer.
 */
interface DashboardRepository {

    /**
     * Emits the current dashboard summary, or null when signed out. Backed by the persisted session,
     * so it survives process death and renders instantly offline — the dashboard must not blank on a
     * warm start while a network call is in flight.
     */
    val summary: Flow<DashboardSummary?>

    /** Re-fetches the profile from the backend (`/auth/me`) and returns the refreshed summary. */
    suspend fun refresh(): AuthResult<DashboardSummary>
}
