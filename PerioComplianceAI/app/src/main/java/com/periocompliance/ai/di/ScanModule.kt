package com.periocompliance.ai.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.periocompliance.ai.BuildConfig
import com.periocompliance.ai.data.remote.ScanApi
import com.periocompliance.ai.data.repository.ScanRepositoryImpl
import com.periocompliance.ai.domain.repository.ScanRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Module 3's wiring. Kept in its own file so adding scans required editing no auth or networking
 * module. It reuses the authenticated [OkHttpClient] and [Json] that NetworkModule already provides
 * — the unqualified client is the one carrying the AuthInterceptor and the token authenticator, so
 * scan requests are authenticated and auto-refresh on 401 exactly like the auth calls.
 */
@Module
@InstallIn(SingletonComponent::class)
object ScanNetworkModule {

    @Provides
    @Singleton
    fun provideScanApi(client: OkHttpClient, json: Json): ScanApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ScanApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ScanModule {

    @Binds
    @Singleton
    abstract fun bindScanRepository(impl: ScanRepositoryImpl): ScanRepository
}
