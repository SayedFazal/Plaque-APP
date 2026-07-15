package com.periocompliance.ai.data.remote

import com.periocompliance.ai.data.remote.dto.ScanResultResponse
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

/** /scans/:scanId endpoints for AI analysis (Module 5). */
interface ScanResultApi {

    @POST("scans/{scanId}/analyze")
    suspend fun analyzeScan(@Path("scanId") scanId: String): ScanResultResponse

    @GET("scans/{scanId}/result")
    suspend fun getResult(@Path("scanId") scanId: String): ScanResultResponse
}
