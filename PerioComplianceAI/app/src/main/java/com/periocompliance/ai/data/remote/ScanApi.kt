package com.periocompliance.ai.data.remote

import com.periocompliance.ai.data.remote.dto.ScanSummaryResponse
import com.periocompliance.ai.data.remote.dto.SubmitScanRequest
import com.periocompliance.ai.data.remote.dto.UploadImageRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * The /scans endpoints (Modules 3 & 4). Served by the same authenticated Retrofit client as [AuthApi] —
 * the access token, refresh-on-401 and error mapping all come for free from Module 1's networking.
 */
interface ScanApi {

    @POST("scans")
    suspend fun submitScan(@Body body: SubmitScanRequest): ScanSummaryResponse

    /** localDate is the device's local calendar day, so the streak is timezone-correct. */
    @GET("scans/summary")
    suspend fun getSummary(@Query("localDate") localDate: String): ScanSummaryResponse

    /**
     * Upload a captured scan image (Module 4).
     *
     * Sends the image as multipart/form-data with metadata. The server stores the image in
     * ScanImage and returns the updated summary (same as /scans POST).
     */
    @POST("scans/image")
    @Multipart
    suspend fun uploadImage(
        @Part("localDate") localDate: String,
        @Part image: MultipartBody.Part,
        @Part("width") width: String? = null,
        @Part("height") height: String? = null,
    ): ScanSummaryResponse
}

/** Build a multipart image for the upload request. */
fun UploadImageRequest.toMultipart(): MultipartBody.Part {
    val body = imageBytes.toRequestBody(mimeType.toMediaType())
    return MultipartBody.Part.createFormData("image", imageName, body)
}
