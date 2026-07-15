package com.periocompliance.ai.domain.model

/** The AI analysis results for a scan (Module 5). */
data class ScanResult(
    val bleeding: Int, // 0-100 (%)
    val inflammation: Int, // 0-100 (%)
    val plaque: Int, // 0-100 (%)
    val overall_score: Int, // 0-100 (%)
    val recommendations: List<String>,
    val provider: String, // "mock", "claude", "openai", etc.
    val modelVersion: String, // e.g., "1.0", "gpt-4-vision"
)
