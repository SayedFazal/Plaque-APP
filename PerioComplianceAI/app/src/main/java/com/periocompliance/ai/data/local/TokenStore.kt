package com.periocompliance.ai.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.periocompliance.ai.data.remote.dto.SessionDto
import com.periocompliance.ai.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Where the session lives now that we mint our own JWTs.
 *
 * This class is the thing I told you not to build when we were on Firebase, and that advice was
 * right then and wrong now. Firebase persisted the session and refreshed the ID token itself, so a
 * hand-rolled token cache would have been a second source of truth that could only ever disagree
 * with the first. With our own backend there is no first source of truth. This is it.
 *
 * Backed by EncryptedSharedPreferences: keys are held in the Android Keystore (hardware-backed on
 * most devices), so the tokens are not readable by a rooted-device dump or an adb backup. Plain
 * SharedPreferences would leave a 30-day refresh token sitting in world-readable XML.
 *
 * The reads are synchronous by design. [AuthInterceptor] and [TokenAuthenticator] run on OkHttp's
 * threads and cannot suspend, so a DataStore Flow would have to be blocked on anyway.
 */
@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext context: Context,
) {

    @Serializable
    private data class CachedUser(
        val uid: String,
        val email: String,
        val displayName: String,
        val isEmailVerified: Boolean,
    )

    private val json = Json { ignoreUnknownKeys = true }

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _user = MutableStateFlow(readUser())

    /** Drives AuthRepository.authState. Emits null the moment the session is cleared. */
    val user: StateFlow<User?> = _user.asStateFlow()

    fun accessToken(): String? = prefs.getString(KEY_ACCESS, null)

    fun refreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    fun cachedUser(): User? = _user.value

    /** Called on register, login, and every token refresh. */
    fun save(session: SessionDto) {
        val user = session.user.toDomain()
        prefs.edit()
            .putString(KEY_ACCESS, session.accessToken)
            .putString(KEY_REFRESH, session.refreshToken)
            .putString(KEY_USER, json.encodeToString(user.toCached()))
            .apply()
        _user.value = user
    }

    /** After /auth/me tells us the address has since been verified. Tokens are untouched. */
    fun updateUser(user: User) {
        prefs.edit()
            .putString(KEY_USER, json.encodeToString(user.toCached()))
            .apply()
        _user.value = user
    }

    fun clear() {
        prefs.edit().clear().apply()
        _user.value = null
    }

    private fun readUser(): User? = prefs.getString(KEY_USER, null)?.let { stored ->
        // A cached user that fails to parse (schema changed, file corrupted) must not crash the
        // splash screen. Treat it as signed out; the worst case is one extra login.
        runCatching { json.decodeFromString<CachedUser>(stored).toDomain() }.getOrNull()
    }

    private fun User.toCached() = CachedUser(uid, email, displayName, isEmailVerified)

    private fun CachedUser.toDomain() = User(uid, email, displayName, isEmailVerified)

    private companion object {
        const val FILE_NAME = "perio_secure_session"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_USER = "user"
    }
}
