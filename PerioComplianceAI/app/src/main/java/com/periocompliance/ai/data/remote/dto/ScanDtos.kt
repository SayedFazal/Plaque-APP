package com.periocompliance.ai.data.remote.dto

import com.periocompliance.ai.domain.model.ScanSummary
import kotlinx.serialization.Serializable

/**
 * Wire format for /scans. Mirrors the backend JSON exactly and is mapped to the domain model in the
 * repository, so a change to the API shape stops at the data layer.
 */

@Serializable
data class SubmitScanRequest(val localDate: String)

@Serializable
data class ScanSummaryResponse(
    val scannedToday: Boolean,
    val streakDays: Int,
    val scansCompleted: Int,
    val complianceScore: Int? = null,
    val lastScanDate: String? = null,
) {
    fun toDomain() = ScanSummary(
        scannedToday = scannedToday,
        streakDays = streakDays,
        scansCompleted = scansCompleted,
        complianceScore = complianceScore,
        lastScanDate = lastScanDate,
    )
}

/** Request body for multipart image upload (Module 4). Not JSON-serializable; built by the API layer. */
data class UploadImageRequest(
    val localDate: String,
    val imageBytes: ByteArray,
    val imageName: String = "scan.jpg",
    val mimeType: String = "image/jpeg",
    val width: Int? = null,
    val height: Int? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UploadImageRequest) return false
        if (localDate != other.localDate) return false
        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (imageName != other.imageName) return false
        if (mimeType != other.mimeType) return false
        if (width != other.width) return false
        if (height != other.height) return false
        return true
    }

    override fun hashCode(): Int {
        var result = localDate.hashCode()
        result = 31 * result + imageBytes.contentHashCode()
        result = 31 * result + imageName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + (width ?: 0)
        result = 31 * result + (height ?: 0)
        return result
    }
}
