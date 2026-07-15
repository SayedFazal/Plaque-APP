package com.periocompliance.ai.data.repository

import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.DashboardSummary
import com.periocompliance.ai.domain.model.DashboardUser
import com.periocompliance.ai.domain.model.ProgressSnapshot
import com.periocompliance.ai.domain.model.ScanResult
import com.periocompliance.ai.domain.model.ScanSummary
import com.periocompliance.ai.domain.model.User
import com.periocompliance.ai.domain.repository.AuthRepository
import com.periocompliance.ai.domain.repository.DashboardRepository
import com.periocompliance.ai.domain.repository.ScanRepository
import com.periocompliance.ai.domain.repository.ScanResultRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Assembles the home dashboard from the signed-in profile (via [AuthRepository]), daily-scan
 * progress (via [ScanRepository]), and latest analysis (via [ScanResultRepository]).
 *
 * [summary] combines all three flows reactively:
 * - Auth session provides user identity
 * - Scan summary provides streak, count, compliance
 * - Analysis result provides the latest health metrics (if analyzed)
 *
 * When a new scan is recorded, the analysis is fetched automatically. If no analysis exists yet,
 * the dashboard shows the progress cards and a "View Results" call-to-action in the analysis area.
 */
@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val scanRepository: ScanRepository,
    private val scanResultRepository: ScanResultRepository,
) : DashboardRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val summary: Flow<DashboardSummary?> =
        combine(
            authRepository.authState,
            scanRepository.summary,
            // When scans exist, watch the analysis result. When no scans, emit null.
            scanRepository.summary.flatMapLatest { scan ->
                if (scan != null && scan.scansCompleted > 0) {
                    scanResultRepository.result
                } else {
                    flowOf(null)
                }
            }.distinctUntilChanged(),
        ) { user, scan, analysis ->
            user?.let {
                DashboardSummary(
                    it.toDashboardUser(),
                    scan.toProgressSnapshot(analysis),
                )
            }
        }

    override suspend fun refresh(): AuthResult<DashboardSummary> {
        val user = when (val result = authRepository.reloadUser()) {
            is AuthResult.Success -> result.data
            is AuthResult.Failure -> return result
        }

        // Scan progress is best-effort: a failure here must not blank a dashboard whose identity
        // loaded fine. Fall back to whatever the scan cache last knew (Empty on a cold start).
        val scan = when (
            val scanResult = scanRepository.refreshSummary(LocalDate.now().toString())
        ) {
            is AuthResult.Success -> scanResult.data
            is AuthResult.Failure -> scanRepository.summary.value
        }

        // Try to fetch the latest analysis if scans exist. This is best-effort; if it fails,
        // we still show progress cards without analysis data.
        val analysis = if (scan != null && scan.scansCompleted > 0) {
            // The ScanResultRepository holds the latest fetched result. If we want fresh data,
            // we'd need to know the latest scanId and call getResult. For now, use the cached result.
            scanResultRepository.result.value
        } else {
            null
        }

        return AuthResult.Success(
            DashboardSummary(
                user.toDashboardUser(),
                scan.toProgressSnapshot(analysis),
            ),
        )
    }
}

private fun User.toDashboardUser(): DashboardUser = DashboardUser(
    fullName = displayName,
    firstName = displayName.trim().substringBefore(' ').ifBlank { displayName },
    email = email,
    isEmailVerified = isEmailVerified,
)

private fun ScanSummary?.toProgressSnapshot(analysis: ScanResult? = null): ProgressSnapshot =
    if (this == null) {
        ProgressSnapshot.Empty
    } else {
        ProgressSnapshot(
            streakDays = streakDays,
            scansCompleted = scansCompleted,
            complianceScore = complianceScore,
            analysisScore = analysis?.overall_score,
            bleeding = analysis?.bleeding,
            inflammation = analysis?.inflammation,
            plaque = analysis?.plaque,
        )
    }
