package com.periocompliance.ai.data.remote

import com.periocompliance.ai.data.remote.dto.ApiErrorEnvelope
import com.periocompliance.ai.domain.model.AuthError
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/**
 * Turns anything the network can throw into an [AuthError].
 *
 * This is the seam that made the migration off Firebase cheap. The old repository mapped
 * FirebaseAuthException codes onto these same cases; this one maps HTTP error codes onto them. No
 * ViewModel, no screen and no test knew the difference, because none of them ever saw a Firebase
 * type or an HTTP type -- they only ever saw AuthError.
 *
 * The `code` strings below are the contract with the backend (src/utils/errors.ts). They must match
 * exactly; a typo here degrades a precise error into a generic "something went wrong".
 */
internal object ApiErrorMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun map(throwable: Throwable): AuthError = when (throwable) {
        // No connection, DNS failure, connection refused (backend not running / adb reverse not
        // set up), read timeout. All of them mean the same thing to the user.
        is IOException -> AuthError.Network

        is HttpException -> fromHttp(throwable)

        else -> AuthError.Unknown(throwable)
    }

    private fun fromHttp(exception: HttpException): AuthError {
        val code = exception.response()
            ?.errorBody()
            ?.string()
            ?.let { body ->
                runCatching { json.decodeFromString<ApiErrorEnvelope>(body).error.code }.getOrNull()
            }

        return when (code) {
            "INVALID_CREDENTIALS" -> AuthError.InvalidCredentials
            "EMAIL_IN_USE" -> AuthError.EmailAlreadyInUse
            "ACCOUNT_DISABLED" -> AuthError.AccountDisabled
            "TOO_MANY_ATTEMPTS" -> AuthError.TooManyAttempts
            "WEAK_PASSWORD" -> AuthError.WeakPassword
            "INVALID_EMAIL", "VALIDATION_FAILED" -> AuthError.InvalidEmail
            "EMAIL_NOT_VERIFIED" -> AuthError.EmailNotVerified
            "SESSION_EXPIRED", "INVALID_TOKEN" -> AuthError.SessionExpired

            // A body we could not parse, or a code we do not know. Fall back on the status, so a
            // proxy returning a bare 502 with an HTML page still reads as a network problem rather
            // than a mysterious failure.
            else -> when (exception.code()) {
                401 -> AuthError.SessionExpired
                403 -> AuthError.AccountDisabled
                409 -> AuthError.EmailAlreadyInUse
                429 -> AuthError.TooManyAttempts
                in 500..599 -> AuthError.Network
                else -> AuthError.Unknown(exception)
            }
        }
    }
}
