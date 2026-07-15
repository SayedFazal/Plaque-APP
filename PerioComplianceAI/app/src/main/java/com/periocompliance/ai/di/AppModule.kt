package com.periocompliance.ai.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.periocompliance.ai.data.local.SessionRepositoryImpl
import com.periocompliance.ai.data.local.sessionDataStore
import com.periocompliance.ai.data.repository.AuthRepositoryImpl
import com.periocompliance.ai.domain.repository.AuthRepository
import com.periocompliance.ai.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Non-secret device flags only: "has seen onboarding", "remember me". Tokens do NOT live here --
     * they are in TokenStore, behind EncryptedSharedPreferences.
     */
    @Provides
    @Singleton
    fun provideSessionDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.sessionDataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    /**
     * The single line that swapped the entire auth backend.
     *
     * AuthRepositoryImpl used to wrap FirebaseAuth; it now wraps Retrofit. Nothing above the
     * data layer had to change, because nothing above it ever knew which one it was.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
}
