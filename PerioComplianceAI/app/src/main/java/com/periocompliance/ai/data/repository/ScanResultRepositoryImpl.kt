package com.periocompliance.ai.data.repository

import com.periocompliance.ai.data.remote.ApiErrorMapper
import com.periocompliance.ai.data.remote.ScanResultApi
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.ScanResult
import com.periocompliance.ai.domain.repository.ScanResultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Fetches and caches scan analysis results from /scans/:scanId endpoints. */
@Singleton
class ScanResultRepositoryImpl @Inject constructor(
    private val api: ScanResultApi,
) : ScanResultRepository {

    private val _result = MutableStateFlow<ScanResult?>(null)
    override val result: StateFlow<ScanResult?> = _result.asStateFlow()

    override suspend fun analyzeScan(scanId: String): AuthResult<ScanResult> = call {
        api.analyzeScan(scanId).toDomain().also { _result.value = it }
    }

    override suspend fun getResult(scanId: String): AuthResult<ScanResult?> = call {
        try {
            api.getResult(scanId).toDomain().also { _result.value = it }
        } catch (e: Exception) {
            // If the result doesn't exist (404), return null rather than an error.
            // The UI layer decides whether "no result" is a "not analyzed yet" state or an error.
            null
        }
    }

    private inline fun <T> call(block: () -> T): AuthResult<T> = runCatching(block).fold(
        onSuccess = { AuthResult.Success(it) },
        onFailure = { AuthResult.Failure(ApiErrorMapper.map(it)) },
    )
}
