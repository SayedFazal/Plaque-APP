package com.periocompliance.ai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.periocompliance.ai.R

/**
 * Type scale transcribed from DESIGN.md. Inter, exclusively -- both design systems say so.
 *
 * res/font/inter_variable.ttf is the Inter variable font (axes: opsz, wght). Each weight below is
 * a named instance on the wght axis, which is why one file covers 400/500/600/700. Variable-font
 * instancing needs API 26, which is the app's minSdk.
 *
 * Typography does NOT change between light and dark. Obsidian Deep's DESIGN.md lists a slightly
 * larger scale (18sp body) because it was authored desktop-first; the mobile scale below is the
 * one that matches the exported phone screens, so it is used for both themes.
 */

// FontVariation is still @ExperimentalTextApi in Compose 1.9. The opt-in is contained to this one
// function; nothing else in the app touches the API.
@OptIn(ExperimentalTextApi::class)
private fun interWeight(weight: Int) = Font(
    resId = R.font.inter_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Inter = FontFamily(
    interWeight(400),
    interWeight(500),
    interWeight(600),
    interWeight(700),
)

// Tokens marked [DESIGN.md] are verbatim. [derived] fills a Material 3 slot the export did not
// name, staying on the same ramp.
val PerioTypography = Typography(
    // display-lg [DESIGN.md] 48/56/700/-0.02em
    displayLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.96).sp,
    ),
    displayMedium = TextStyle( // [derived]
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.72).sp,
    ),
    displaySmall = TextStyle( // [derived]
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.32).sp,
    ),
    // headline-lg [DESIGN.md] 32/40/600/-0.01em
    headlineLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.32).sp,
    ),
    // headline-lg-mobile [DESIGN.md] 24/32/600 -- the workhorse screen title on phones
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle( // [derived]
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    // title-md [DESIGN.md] 20/28/600
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle( // [derived] card headers
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle( // [derived]
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // body-lg [DESIGN.md] 16/24/400
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // body-md [DESIGN.md] 14/20/400
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle( // [derived]
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle( // [derived] button text
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // label-md [DESIGN.md] 12/16/500/0.01em
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.12.sp,
    ),
    // label-sm [DESIGN.md] 11/16/600
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
)
