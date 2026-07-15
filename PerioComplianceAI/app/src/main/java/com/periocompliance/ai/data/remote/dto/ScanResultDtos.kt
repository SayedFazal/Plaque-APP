package com.periocompliance.ai.data.remote.dto

import com.periocompliance.ai.domain.model.ScanResult
import kotlinx.serialization.Serializable

@Serializable
data class ScanResultResponse(
    val bleeding: Int,
    val inflammation: Int,
    val plaque: Int,
    val overall_score: Int,
    val recommendations: List<String>,
    val provider: String,
    val modelVersion: String,
) {
    fun toDomain() = ScanResult(
        bleeding = bleeding,
        inflammation = inflammation,
        plaque = plaque,
        overall_score = overall_score,
        recommendations = recommendations,
        provider = provider,
        modelVersion = modelVersion,
    )
}
