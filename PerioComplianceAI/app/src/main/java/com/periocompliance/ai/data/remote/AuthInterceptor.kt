package com.periocompliance.ai.data.remote

import com.periocompliance.ai.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches `Authorization: Bearer <accessToken>` to every request that needs it.
 *
 * The public endpoints are skipped deliberately. Sending a stale token to /auth/login would be
 * harmless but pointless; sending one to /auth/refresh would be actively confusing, since that
 * endpoint authenticates with the refresh token in the body.
 */
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.encodedPath.isPublic()) {
            return chain.proceed(request)
        }

        val token = tokenStore.accessToken()
            ?: return chain.proceed(request)

        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build(),
        )
    }
}

/**
 * Endpoints that take no access token. Shared with [TokenAuthenticator], which must never try to
 * refresh in response to one of these: a 401 from /auth/login means "wrong password", and
 * attempting a token refresh because of it would be nonsense.
 */
internal fun String.isPublic(): Boolean = PUBLIC_PATHS.any { endsWith(it) }

private val PUBLIC_PATHS = listOf(
    "/auth/register",
    "/auth/login",
    "/auth/refresh",
    "/auth/forgot-password",
    "/auth/reset-password",
    "/auth/resend-verification",
)
