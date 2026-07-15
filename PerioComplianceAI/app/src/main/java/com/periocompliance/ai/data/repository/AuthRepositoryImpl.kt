package com.periocompliance.ai.data.repository

import com.periocompliance.ai.data.local.TokenStore
import com.periocompliance.ai.data.remote.ApiErrorMapper
import com.periocompliance.ai.data.remote.AuthApi
import com.periocompliance.ai.data.remote.dto.EmailRequest
import com.periocompliance.ai.data.remote.dto.LoginRequest
import com.periocompliance.ai.data.remote.dto.RefreshRequest
import com.periocompliance.ai.data.remote.dto.RegisterRequest
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.User
import com.periocompliance.ai.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backed by our own backend now, not Firebase.
 *
 * Everything above this file is untouched by that swap -- the ViewModels, the screens, the use
 * cases, the validators and all 19 unit tests. They only ever knew [AuthRepository] and
 * [AuthError]. This is what the layering was for.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
) : AuthRepository {

    override val authState: Flow<User?> = tokenStore.user

    /**
     * Read from encrypted storage, not the network. That is what makes "stay logged in after
     * restart" work offline and instantly -- the splash screen cannot afford to wait on an HTTP
     * round trip, and would have to show a spinner in airplane mode.
     */
    override fun currentUser(): User? = tokenStore.cachedUser()

    override suspend fun signIn(email: String, password: String): AuthResult<User> = call {
        val session = api.login(LoginRequest(email, password))
        tokenStore.save(session)
        session.user.toDomain()
    }

    /**
     * The backend returns a session here, and it is deliberately a session that cannot do anything:
     * every endpoint holding clinical data is behind `requireVerified` server-side. What it *can*
     * do is call /auth/me, which is the only way the verification screen can ever find out that the
     * user has clicked the link in their inbox.
     *
     * The backend also sends the verification email itself, which is why -- unlike the Firebase
     * version -- RegisterUseCase no longer asks for one.
     */
    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): AuthResult<User> = call {
        val session = api.register(RegisterRequest(name, email, password))
        tokenStore.save(session)
        session.user.toDomain()
    }

    override suspend fun sendPasswordReset(email: String): AuthResult<Unit> = call {
        // The backend returns 204 for an unknown address as well as a known one, on purpose: a 404
        // here would let anyone check which dentists have accounts. So there is nothing to special
        // case on this side -- unlike the Firebase version, which had to swallow USER_NOT_FOUND.
        api.forgotPassword(EmailRequest(email))
    }

    override suspend fun sendVerificationEmail(): AuthResult<Unit> {
        val email = tokenStore.cachedUser()?.email
            ?: return AuthResult.Failure(AuthError.SessionExpired)

        return call { api.resendVerification(EmailRequest(email)) }
    }

    /**
     * Asks the server who we are. The cached user says `isEmailVerified = false` from the moment of
     * registration and has no way of learning otherwise -- clicking a link in a browser does not
     * notify the phone. This is the call that closes that gap, and it is why the verification screen
     * has an explicit "I've verified" button.
     */
    override suspend fun reloadUser(): AuthResult<User> = call {
        val user = api.me().user.toDomain()
        tokenStore.updateUser(user)
        user
    }

    override suspend fun signOut() {
        // Tell the server first so the refresh token is actually revoked rather than left alive for
        // 30 days. If the call fails (offline), clear locally anyway: the user asked to be signed
        // out, and refusing to do so because the network is down would be absurd.
        tokenStore.refreshToken()?.let { token ->
            runCatching { api.logout(RefreshRequest(token)) }
        }
        tokenStore.clear()
    }

    /** Every network call funnels through here, so no exception can escape the data layer. */
    private inline fun <T> call(block: () -> T): AuthResult<T> = runCatching(block).fold(
        onSuccess = { AuthResult.Success(it) },
        onFailure = { AuthResult.Failure(ApiErrorMapper.map(it)) },
    )
}
