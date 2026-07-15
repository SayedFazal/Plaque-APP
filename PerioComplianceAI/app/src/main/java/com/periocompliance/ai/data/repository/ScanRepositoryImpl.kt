package com.periocompliance.ai.data.repository

import com.periocompliance.ai.data.remote.ApiErrorMapper
import com.periocompliance.ai.data.remote.ScanApi
import com.periocompliance.ai.data.remote.dto.SubmitScanRequest
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.ScanSummary
import com.periocompliance.ai.domain.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Talks to /scans over the authenticated client, and keeps the latest [ScanSummary] in a hot flow so
 * the dashboard reacts to a new scan without polling.
 *
 * Error handling is deliberately identical to [AuthRepositoryImpl]: every call funnels through [call],
 * so the same [ApiErrorMapper] turns any HTTP/IO failure into the app-wide [com.periocompliance.ai.domain.model.AuthError].
 * Reusing that seam is why a scan request shows the same "no connection" copy as a login does.
 */
@Singleton
class ScanRepositoryImpl @Inject constructor(
    private val api: ScanApi,
) : ScanRepository {

    private val _summary = MutableStateFlow<ScanSummary?>(null)
    override val summary: StateFlow<ScanSummary?> = _summary.asStateFlow()

    override suspend fun submitScan(localDate: String): AuthResult<ScanSummary> = call {
        api.submitScan(SubmitScanRequest(localDate)).toDomain().also { _summary.value = it }
    }

    override suspend fun refreshSummary(localDate: String): AuthResult<ScanSummary> = call {
        api.getSummary(localDate).toDomain().also { _summary.value = it }
    }

    private inline fun <T> call(block: () -> T): AuthResult<T> = runCatching(block).fold(
        onSuccess = { AuthResult.Success(it) },
        onFailure = { AuthResult.Failure(ApiErrorMapper.map(it)) },
    )
}
