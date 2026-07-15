package com.periocompliance.ai.data.remote

import com.periocompliance.ai.data.local.TokenStore
import com.periocompliance.ai.data.remote.dto.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

/**
 * Silent token refresh. When a request comes back 401 because the 15-minute access token expired,
 * this swaps in a fresh one and OkHttp replays the request. The user never sees it.
 *
 * Three things here are load-bearing and easy to get wrong:
 *
 * 1. [refreshApi] is a *separate* Retrofit instance with no authenticator attached. If the refresh
 *    call went through this same client, a 401 on refresh would invoke this authenticator, which
 *    would call refresh, which would 401... until the stack blew up.
 *
 * 2. The whole thing is synchronized, and re-reads the token inside the lock. Two requests failing
 *    at once would otherwise both refresh; the second would present the token the first had already
 *    rotated away, the backend would treat that reuse as theft (it does -- see auth.service.ts) and
 *    revoke every session on the account. The user gets logged out for being on a good connection.
 *
 * 3. `responseCount` caps retries. Without it, a backend that returns 401 for a reason refresh
 *    cannot fix would loop forever.
 *
 * runBlocking is correct here, not a shortcut: OkHttp's Authenticator API is synchronous and is
 * already running on a background thread.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    // Provider, not the instance: the refresh Retrofit is built from an OkHttp client that this
    // authenticator is not part of, and injecting it directly would be a dependency cycle.
    private val refreshApi: Provider<AuthApi>,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.isPublic()) return null
        if (responseCount(response) >= MAX_RETRIES) return null

        val staleToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        synchronized(this) {
            val currentToken = tokenStore.accessToken()

            // Another thread already refreshed while we were waiting for the lock. Just retry with
            // what it got.
            if (currentToken != null && currentToken != staleToken) {
                return response.request.retryWith(currentToken)
            }

            val refreshToken = tokenStore.refreshToken() ?: return null

            val session = runCatching {
                runBlocking { refreshApi.get().refresh(RefreshRequest(refreshToken)) }
            }.getOrNull()

            if (session == null) {
                // The refresh token is expired, revoked, or the server rejected it. There is no way
                // back from here: drop the session so the app returns to the login screen rather
                // than retrying a request that can never succeed.
                tokenStore.clear()
                return null
            }

            tokenStore.save(session)
            return response.request.retryWith(session.accessToken)
        }
    }

    private fun Request.retryWith(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        const val MAX_RETRIES = 2
    }
}
