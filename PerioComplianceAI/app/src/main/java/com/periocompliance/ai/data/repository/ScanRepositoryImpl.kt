package com.periocompliance.ai.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.periocompliance.ai.data.remote.ApiErrorMapper
import com.periocompliance.ai.data.remote.ScanApi
import com.periocompliance.ai.data.remote.dto.SubmitScanRequest
import com.periocompliance.ai.data.remote.dto.UploadImageRequest
import com.periocompliance.ai.data.remote.toMultipart
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.ScanSummary
import com.periocompliance.ai.domain.repository.ScanRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
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
    @ApplicationContext private val context: Context,
) : ScanRepository {

    private val _summary = MutableStateFlow<ScanSummary?>(null)
    override val summary: StateFlow<ScanSummary?> = _summary.asStateFlow()

    override suspend fun submitScan(localDate: String): AuthResult<ScanSummary> = call {
        api.submitScan(SubmitScanRequest(localDate)).toDomain().also { _summary.value = it }
    }

    override suspend fun refreshSummary(localDate: String): AuthResult<ScanSummary> = call {
        api.getSummary(localDate).toDomain().also { _summary.value = it }
    }

    override suspend fun submitScanWithImage(localDate: String, imageUri: Uri): AuthResult<ScanSummary> =
        call {
            val (bytes, width, height) = readAndCompressImage(imageUri)
            val request =
                UploadImageRequest(
                    localDate = localDate,
                    imageBytes = bytes,
                    width = width,
                    height = height,
                )
            api.uploadImage(
                localDate = localDate,
                image = request.toMultipart(),
                width = width?.toString(),
                height = height?.toString(),
            )
                .toDomain()
                .also { _summary.value = it }
        }

    private fun readAndCompressImage(uri: Uri): Triple<ByteArray, Int?, Int?> {
        // Read the image bounds to detect rotation/orientation
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, opts) }

        val origWidth = opts.outWidth
        val origHeight = opts.outHeight

        // Downsample if needed; aim for ~500px on the long side for gum photos (plenty of detail)
        var sampleSize = 1
        val reqSize = 500
        while (origWidth / sampleSize > reqSize || origHeight / sampleSize > reqSize) {
            sampleSize *= 2
        }

        // Decode the downsampled bitmap
        val bitmap = context.contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        } ?: return Triple(byteArrayOf(), null, null)

        // Compress to JPEG
        val out = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
        bitmap.recycle()

        return Triple(out.toByteArray(), bitmap.width, bitmap.height)
    }

    private inline fun <T> call(block: () -> T): AuthResult<T> = runCatching(block).fold(
        onSuccess = { AuthResult.Success(it) },
        onFailure = { AuthResult.Failure(ApiErrorMapper.map(it)) },
    )
}
