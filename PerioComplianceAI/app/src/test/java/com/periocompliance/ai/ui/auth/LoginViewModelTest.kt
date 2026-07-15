package com.periocompliance.ai.ui.auth

import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.model.AuthResult
import com.periocompliance.ai.domain.model.User
import com.periocompliance.ai.domain.repository.AuthRepository
import com.periocompliance.ai.domain.repository.SessionRepository
import com.periocompliance.ai.domain.usecase.SignInUseCase
import com.periocompliance.ai.domain.validation.FieldError
import com.periocompliance.ai.ui.auth.login.LoginStatus
import com.periocompliance.ai.ui.auth.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Hand-written fakes rather than a mocking library: the interfaces are small, and a fake that
 * records what it was called with reads better in a failure message than a verify() stack trace.
 */
private class FakeAuthRepository : AuthRepository {
    var signInResult: AuthResult<User> = AuthResult.Success(VERIFIED_USER)
    var lastEmail: String? = null
    var lastPassword: String? = null

    override val authState: Flow<User?> = MutableStateFlow(null)
    override fun currentUser(): User? = null

    override suspend fun signIn(email: String, password: String): AuthResult<User> {
        lastEmail = email
        lastPassword = password
        return signInResult
    }

    override suspend fun register(name: String, email: String, password: String) =
        AuthResult.Success(VERIFIED_USER)

    override suspend fun sendPasswordReset(email: String) = AuthResult.Success(Unit)
    override suspend fun sendVerificationEmail() = AuthResult.Success(Unit)
    override suspend fun reloadUser() = AuthResult.Success(VERIFIED_USER)
    override suspend fun signOut() = Unit

    companion object {
        val VERIFIED_USER = User("uid", "dr@clinic.com", "Dr Smith", isEmailVerified = true)
        val UNVERIFIED_USER = VERIFIED_USER.copy(isEmailVerified = false)
    }
}

private class FakeSessionRepository : SessionRepository {
    var rememberMeValue = false
    override val hasSeenOnboarding: Flow<Boolean> = MutableStateFlow(true)
    override suspend fun setOnboardingSeen() = Unit
    override val rememberMe: Flow<Boolean> = MutableStateFlow(false)
    override suspend fun setRememberMe(enabled: Boolean) { rememberMeValue = enabled }
    override suspend fun clear() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var auth: FakeAuthRepository
    private lateinit var session: FakeSessionRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        auth = FakeAuthRepository()
        session = FakeSessionRepository()
        viewModel = LoginViewModel(SignInUseCase(auth, session))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submit is blocked until both fields are filled`() {
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onEmailChange("dr@clinic.com")
        assertFalse("email alone is not enough", viewModel.uiState.value.canSubmit)

        viewModel.onPasswordChange("anything")
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `a malformed email blocks submit and shows a field error`() {
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("anything")

        assertEquals(FieldError.EmailMalformed, viewModel.uiState.value.emailError)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `login does not enforce registration password rules`() = runTest(dispatcher) {
        // An account created before the complexity rules existed must still be able to sign in.
        viewModel.onEmailChange("dr@clinic.com")
        viewModel.onPasswordChange("oldweak")

        assertTrue(viewModel.uiState.value.canSubmit)

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
    }

    @Test
    fun `a successful sign in reaches Success and persists remember me`() = runTest(dispatcher) {
        viewModel.onEmailChange("dr@clinic.com")
        viewModel.onPasswordChange("Str0ng!Pass")
        viewModel.onRememberMeChange(true)

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
        assertTrue(session.rememberMeValue)
    }

    @Test
    fun `the email is trimmed before it reaches the repository`() = runTest(dispatcher) {
        viewModel.onEmailChange("  dr@clinic.com  ")
        viewModel.onPasswordChange("Str0ng!Pass")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals("dr@clinic.com", auth.lastEmail)
    }

    @Test
    fun `an unverified account is routed to the gate, not signed in`() = runTest(dispatcher) {
        auth.signInResult = AuthResult.Success(FakeAuthRepository.UNVERIFIED_USER)

        viewModel.onEmailChange("dr@clinic.com")
        viewModel.onPasswordChange("Str0ng!Pass")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(LoginStatus.NeedsVerification, viewModel.uiState.value.status)
    }

    @Test
    fun `wrong credentials surface as a mapped failure`() = runTest(dispatcher) {
        auth.signInResult = AuthResult.Failure(AuthError.InvalidCredentials)

        viewModel.onEmailChange("dr@clinic.com")
        viewModel.onPasswordChange("wrong")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(
            LoginStatus.Failed(AuthError.InvalidCredentials),
            viewModel.uiState.value.status,
        )
    }

    @Test
    fun `editing a field dismisses the previous failure`() = runTest(dispatcher) {
        auth.signInResult = AuthResult.Failure(AuthError.InvalidCredentials)
        viewModel.onEmailChange("dr@clinic.com")
        viewModel.onPasswordChange("wrong")
        viewModel.onSubmit()
        advanceUntilIdle()

        viewModel.onPasswordChange("wrong2")

        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
    }
}
