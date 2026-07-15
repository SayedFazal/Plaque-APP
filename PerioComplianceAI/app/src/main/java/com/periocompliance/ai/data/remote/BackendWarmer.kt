package com.periocompliance.ai.data.remote

import com.periocompliance.ai.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Named
import javax.inject.Singleton
import javax.inject.Inject

/**
 * Wakes the deployed backend as early as possible.
 *
 * Render's free tier spins the service down when idle; the next request pays a 30–50s cold-start.
 * Firing a throwaway GET /health the moment the process starts means the backend is (or is on its
 * way to being) awake by the time the user has typed their credentials, so the real login/register
 * call is far less likely to be the one that eats the cold start.
 *
 * This is strictly best-effort: it runs on a background coroutine, never blocks the UI, and any
 * failure (offline, timeout, still-cold) is swallowed. It changes no state and no navigation — the
 * real auth requests remain the source of truth for success and error handling.
 *
 * It deliberately uses the interceptor-free ["refresh"][javax.inject.Named] client and raw OkHttp,
 * so the ping carries no Authorization header, never trips the token authenticator, and does no
 * response deserialization.
 */
@Singleton
class BackendWarmer @Inject constructor(
    @Named("refresh") private val client: OkHttpClient,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // BuildConfig.API_BASE_URL ends in a slash (Retrofit requires it), so this is a clean join.
    private val healthUrl = BuildConfig.API_BASE_URL + "health"

    fun warmUp() {
        scope.launch {
            try {
                val request = Request.Builder().url(healthUrl).get().build()
                // .use closes the body so the connection is released; the payload itself is ignored.
                client.newCall(request).execute().use { /* result intentionally unused */ }
            } catch (_: Exception) {
                // Best-effort wake-up. If it fails, the normal app flow continues unchanged.
            }
        }
    }
}
