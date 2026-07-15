package com.periocompliance.ai

import android.app.Application
import com.periocompliance.ai.data.remote.BackendWarmer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PerioComplianceApp : Application() {

    // Hilt field-injects this during super.onCreate(), so it is ready to use below.
    @Inject
    lateinit var backendWarmer: BackendWarmer

    override fun onCreate() {
        super.onCreate()
        // Fire a silent, non-blocking GET /health to wake Render's free tier before the user
        // reaches the login screen. Failures are ignored inside warmUp().
        backendWarmer.warmUp()
    }
}
