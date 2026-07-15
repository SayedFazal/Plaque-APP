package com.periocompliance.ai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.periocompliance.ai.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "perio_session")

/**
 * Device-scoped flags. There are no credentials in here, and there must never be: Firebase already
 * persists the session and refreshes the ID token, so a hand-rolled token cache would be a second
 * source of truth that can only ever disagree with the first.
 */
@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SessionRepository {

    private object Keys {
        val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    // A corrupt preferences file must not take the app down on the splash screen; an empty read
    // just means the user sees onboarding again.
    private val preferences: Flow<Preferences> = dataStore.data.catch { throwable ->
        if (throwable is IOException) emit(emptyPreferences()) else throw throwable
    }

    override val hasSeenOnboarding: Flow<Boolean> =
        preferences.map { it[Keys.ONBOARDING_SEEN] ?: false }

    override suspend fun setOnboardingSeen() {
        dataStore.edit { it[Keys.ONBOARDING_SEEN] = true }
    }

    override val rememberMe: Flow<Boolean> =
        preferences.map { it[Keys.REMEMBER_ME] ?: false }

    override suspend fun setRememberMe(enabled: Boolean) {
        dataStore.edit { it[Keys.REMEMBER_ME] = enabled }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
