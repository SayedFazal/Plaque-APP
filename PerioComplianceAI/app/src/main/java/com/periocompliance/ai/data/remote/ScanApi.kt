package com.periocompliance.ai.data.remote

import com.periocompliance.ai.data.remote.dto.ScanSummaryResponse
import com.periocompliance.ai.data.remote.dto.SubmitScanRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * The /scans endpoints (Module 3). Served by the same authenticated Retrofit client as [AuthApi] —
 * the access token, refresh-on-401 and error mapping all come for free from Module 1's networking.
 */
interface ScanApi {

    @POST("scans")
    suspend fun submitScan(@Body body: SubmitScanRequest): ScanSummaryResponse

    /** localDate is the device's local calendar day, so the streak is timezone-correct. */
    @GET("scans/summary")
    suspend fun getSummary(@Query("localDate") localDate: String): ScanSummaryResponse
}
