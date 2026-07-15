package com.periocompliance.ai.domain.usecase

import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.User
import com.periocompliance.ai.domain.repository.AuthRepository
import com.periocompliance.ai.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * One class per action. They are thin on purpose -- a use case earns its keep when it holds a rule
 * that is not the repository's business, and most of these hold exactly one.
 */

class SignInUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val session: SessionRepository,
) {
    /**
     * The rule that lives here rather than in the repository: signing in successfully is not the
     * same as being allowed in. An unverified account is bounced back out, because the spec says
     * the user cannot continue until the address is confirmed.
     */
    suspend operator fun invoke(
        email: String,
        password: String,
        rememberMe: Boolean,
    ): AuthResult<User> {
        val result = auth.signIn(email.trim(), password)
        if (result is AuthResult.Success) {
            session.setRememberMe(rememberMe)
            if (!result.data.isEmailVerified) {
                return AuthResult.Failure(AuthError.EmailNotVerified)
            }
        }
        return result
    }
}

class RegisterUseCase @Inject constructor(
    private val auth: AuthRepository,
) {
    /**
     * The backend sends the verification email as part of registration. The Firebase version had to
     * ask for one explicitly, because Firebase created the account silently; asking again here now
     * would simply deliver the user two identical emails.
     */
    suspend operator fun invoke(name: String, email: String, password: String): AuthResult<User> =
        auth.register(name.trim(), email.trim(), password)
}

class SendPasswordResetUseCase @Inject constructor(
    private val auth: AuthRepository,
) {
    suspend operator fun invoke(email: String): AuthResult<Unit> =
        auth.sendPasswordReset(email.trim())
}

class SendVerificationEmailUseCase @Inject constructor(
    private val auth: AuthRepository,
) {
    suspend operator fun invoke(): AuthResult<Unit> = auth.sendVerificationEmail()
}

class RefreshVerificationStatusUseCase @Inject constructor(
    private val auth: AuthRepository,
) {
    suspend operator fun invoke(): AuthResult<User> = auth.reloadUser()
}

class ObserveAuthStateUseCase @Inject constructor(
    private val auth: AuthRepository,
) {
    operator fun invoke(): Flow<User?> = auth.authState
}

class SignOutUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val session: SessionRepository,
) {
    /**
     * Clears the session flags as well as the Firebase session. Leaving rememberMe set after a
     * logout is the kind of thing nobody notices until a shared clinic tablet leaks a session.
     * Onboarding-seen deliberately survives: it is a property of the device, not the account.
     */
    suspend operator fun invoke() {
        auth.signOut()
        session.setRememberMe(false)
    }
}
