package com.periocompliance.ai.domain.usecase

import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.DashboardSummary
import com.periocompliance.ai.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Module 2's use cases. Thin, like the auth ones: they exist so the ViewModel depends on verbs
 * ("observe the dashboard", "refresh the dashboard") rather than on a repository surface.
 */

class ObserveDashboardUseCase @Inject constructor(
    private val repository: DashboardRepository,
) {
    operator fun invoke(): Flow<DashboardSummary?> = repository.summary
}

class RefreshDashboardUseCase @Inject constructor(
    private val repository: DashboardRepository,
) {
    suspend operator fun invoke(): AuthResult<DashboardSummary> = repository.refresh()
}
