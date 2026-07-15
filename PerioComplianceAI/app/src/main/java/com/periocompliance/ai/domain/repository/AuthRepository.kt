package com.periocompliance.ai.domain.repository

import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * The only door to authentication. Everything above this interface is provider-agnostic; if
 * Firebase were swapped for a custom backend tomorrow, nothing outside data/ would change.
 *
 * Note what is absent: there is no getToken(), no refreshToken(), no saveJwt(). Firebase already
 * persists the session and refreshes the ID token behind FirebaseAuth.currentUser. Re-implementing
 * that with a hand-rolled token cache is a day of work that buys a race condition.
 */
interface AuthRepository {

    /** Emits the signed-in user, or null when signed out. Survives process death. */
    val authState: Flow<User?>

    /** Cheap synchronous read for the splash decision; null when signed out. */
    fun currentUser(): User?

    suspend fun signIn(email: String, password: String): AuthResult<User>

    suspend fun register(name: String, email: String, password: String): AuthResult<User>

    suspend fun sendPasswordReset(email: String): AuthResult<Unit>

    suspend fun sendVerificationEmail(): AuthResult<Unit>

    /**
     * Re-fetches the user from the server. Needed because [User.isEmailVerified] is baked into the
     * cached token: a user who clicks the verification link is still "unverified" locally until
     * this runs.
     */
    suspend fun reloadUser(): AuthResult<User>

    suspend fun signOut()
}

/** Session-scoped flags. Not credentials -- Firebase owns those. */
interface SessionRepository {
    val hasSeenOnboarding: Flow<Boolean>
    suspend fun setOnboardingSeen()

    val rememberMe: Flow<Boolean>
    suspend fun setRememberMe(enabled: Boolean)

    suspend fun clear()
}

/** Sugar for `is AuthResult.Failure` at call sites. */
fun <T> AuthResult<T>.errorOrNull(): AuthError? =
    (this as? AuthResult.Failure)?.error
