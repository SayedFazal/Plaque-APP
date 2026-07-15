package com.periocompliance.ai.data.repository

import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.DashboardSummary
import com.periocompliance.ai.domain.model.DashboardUser
import com.periocompliance.ai.domain.model.ProgressSnapshot
import com.periocompliance.ai.domain.model.ScanSummary
import com.periocompliance.ai.domain.model.User
import com.periocompliance.ai.domain.repository.AuthRepository
import com.periocompliance.ai.domain.repository.DashboardRepository
import com.periocompliance.ai.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Assembles the home dashboard from the signed-in profile (via [AuthRepository]) and the daily-scan
 * progress (via [ScanRepository]).
 *
 * As of Module 3 the progress is real. [summary] combines the auth session with the scan repository's
 * hot summary flow, so the moment a scan is recorded the "No scans yet" card flips to live streak /
 * count / compliance numbers with no work in the ViewModel — the mapping to [ProgressSnapshot] is the
 * only place that knows scans became real. This module still adds no networking and touches no auth
 * code; it only consumes the two repositories' public interfaces.
 */
@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val scanRepository: ScanRepository,
) : DashboardRepository {

    override val summary: Flow<DashboardSummary?> =
        combine(authRepository.authState, scanRepository.summary) { user, scan ->
            user?.let { DashboardSummary(it.toDashboardUser(), scan.toProgressSnapshot()) }
        }

    override suspend fun refresh(): AuthResult<DashboardSummary> {
        val user = when (val result = authRepository.reloadUser()) {
            is AuthResult.Success -> result.data
            is AuthResult.Failure -> return result
        }

        // Scan progress is best-effort: a failure here must not blank a dashboard whose identity
        // loaded fine. Fall back to whatever the scan cache last knew (Empty on a cold start).
        val progress = when (
            val scanResult = scanRepository.refreshSummary(LocalDate.now().toString())
        ) {
            is AuthResult.Success -> scanResult.data.toProgressSnapshot()
            is AuthResult.Failure -> scanRepository.summary.value.toProgressSnapshot()
        }

        return AuthResult.Success(DashboardSummary(user.toDashboardUser(), progress))
    }
}

private fun User.toDashboardUser(): DashboardUser = DashboardUser(
    fullName = displayName,
    firstName = displayName.trim().substringBefore(' ').ifBlank { displayName },
    email = email,
    isEmailVerified = isEmailVerified,
)

private fun ScanSummary?.toProgressSnapshot(): ProgressSnapshot =
    if (this == null) {
        ProgressSnapshot.Empty
    } else {
        ProgressSnapshot(
            streakDays = streakDays,
            scansCompleted = scansCompleted,
            complianceScore = complianceScore,
        )
    }
