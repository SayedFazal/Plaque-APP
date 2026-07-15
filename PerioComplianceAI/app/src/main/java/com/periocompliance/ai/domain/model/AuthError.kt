package com.periocompliance.ai.domain.model

/**
 * Every way authentication can fail, enumerated.
 *
 * The point of this type is that a raw exception message never reaches the UI. Firebase throws
 * things like "The supplied auth credential is incorrect, malformed or has expired." -- true, and
 * useless to a dentist. The repository maps error codes onto these cases and the UI maps these
 * cases onto strings.xml.
 *
 * [Unknown] carries the original throwable for logging, never for display.
 */
sealed interface AuthError {
    /** No connection, or the request timed out. Retryable. */
    data object Network : AuthError

    /** Wrong password, unknown email, or a malformed credential. Deliberately one case: telling
     *  an attacker which of the three it was is an account-enumeration hole. */
    data object InvalidCredentials : AuthError

    data object EmailAlreadyInUse : AuthError

    data object AccountDisabled : AuthError

    /** Firebase rate-limited this device after repeated failures. */
    data object TooManyAttempts : AuthError

    /** Firebase rejected the password independently of our own rules. */
    data object WeakPassword : AuthError

    data object InvalidEmail : AuthError

    /** Signed in, but the address has not been confirmed. Gates the main graph. */
    data object EmailNotVerified : AuthError

    /** Session expired or was revoked; the user must sign in again. */
    data object SessionExpired : AuthError

    data class Unknown(val cause: Throwable?) : AuthError
}

/** Thin wrapper so use cases can return either side without exceptions crossing layers. */
sealed interface AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>
    data class Failure(val error: AuthError) : AuthResult<Nothing>
}
