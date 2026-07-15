package com.periocompliance.ai.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.periocompliance.ai.BuildConfig
import com.periocompliance.ai.data.remote.ScanResultApi
import com.periocompliance.ai.data.repository.ScanResultRepositoryImpl
import com.periocompliance.ai.domain.repository.ScanResultRepository
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

@Module
@InstallIn(SingletonComponent::class)
object ScanResultNetworkModule {
    @Provides
    @Singleton
    fun provideScanResultApi(client: OkHttpClient, json: Json): ScanResultApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ScanResultApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ScanResultModule {
    @Binds
    @Singleton
    abstract fun bindScanResultRepository(
        impl: ScanResultRepositoryImpl,
    ): ScanResultRepository
}
