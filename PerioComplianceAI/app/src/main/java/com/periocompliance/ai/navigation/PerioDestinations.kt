package com.periocompliance.ai.navigation

/**
 * Every destination in the app, named after the folder it was exported from in
 * stitch_periocompliance_ai_dental_app/. Declared up front so each module drops into a slot that
 * already exists instead of reshaping the graph.
 *
 * The module number next to each route is the phase that implements it. Nothing outside the module
 * currently being built should be touched.
 */
object Routes {

    // Temporary. Start destination until Module 1 lands; see PerioNavHost.
    const val DESIGN_SYSTEM = "design_system"

    // --- Module 1: Authentication (built) -----------------------------------------
    const val SPLASH = "splash"

    /**
     * The three exported onboarding screens (track, ai_analysis, compliance) are one destination,
     * not three: they are a swipeable pager, so routing between them would put two back-stack
     * entries between the user and the login screen for no reason.
     */
    const val ONBOARDING = "onboarding"

    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val FORGOT_PASSWORD = "auth/forgot_password"
    const val VERIFY_EMAIL = "auth/verify_email"

    // --- Module 2: Patient home dashboard ----------------------------------------
    const val HOME_DASHBOARD = "home"

    // --- Module 3 + 4: Camera capture and upload ---------------------------------
    const val DAILY_SCAN = "scan"

    // --- Module 5: Gemini analysis result ----------------------------------------
    const val AI_RESULT = "scan/result/{scanId}"
    fun aiResult(scanId: String) = "scan/result/$scanId"

    // --- Module 6: History --------------------------------------------------------
    const val HISTORY = "history"

    // --- Module 7: Charts ---------------------------------------------------------
    const val PROGRESS_DASHBOARD = "progress"

    // --- Module 8: Notifications --------------------------------------------------
    const val NOTIFICATIONS = "notifications"

    // --- Module 9: Dentist ---------------------------------------------------------
    const val DENTIST_DASHBOARD = "dentist"

    const val PROFILE = "profile"

    /** Graph root for the signed-in app, so logout can pop the whole thing in one call. */
    object Graph {
        const val MAIN = "graph/main"
    }
}
