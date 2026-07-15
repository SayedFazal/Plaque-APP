package com.periocompliance.ai.di

import com.periocompliance.ai.BuildConfig
import com.periocompliance.ai.data.local.TokenStore
import com.periocompliance.ai.data.remote.AuthApi
import com.periocompliance.ai.data.remote.AuthInterceptor
import com.periocompliance.ai.data.remote.TokenAuthenticator
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // The backend will grow fields (Module 2 onward). An unknown key must not crash an older
        // client that has not been updated yet.
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        // BODY in debug would print the access token, the refresh token AND the plaintext password
        // on every login into logcat, where any app with READ_LOGS can see it. HEADERS is enough to
        // see what happened; the body of an auth request is exactly what you must not log.
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    /**
     * The client used for the refresh call, and nothing else.
     *
     * It has no [TokenAuthenticator], and that is the entire point: if refreshing went through the
     * authenticated client, a 401 on refresh would trigger the authenticator, which would refresh,
     * which would 401, forever.
     */
    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshApi(
        @Named("refresh") client: OkHttpClient,
        json: Json,
    ): AuthApi = retrofit(client, json).create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenStore: TokenStore) = AuthInterceptor(tokenStore)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenStore: TokenStore,
        @Named("refresh") refreshApi: Provider<AuthApi>,
    ) = TokenAuthenticator(tokenStore, refreshApi)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        authenticator: TokenAuthenticator,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .authenticator(authenticator)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(client: OkHttpClient, json: Json): AuthApi =
        retrofit(client, json).create(AuthApi::class.java)

    private fun retrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        // Debug: http://127.0.0.1:3000/ -- the device's own loopback, forwarded to the laptop by
        // `adb reverse`. Release: the deployed HTTPS URL. See app/build.gradle.kts.
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
