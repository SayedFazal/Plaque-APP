package com.periocompliance.ai.di

import com.periocompliance.ai.data.repository.DashboardRepositoryImpl
import com.periocompliance.ai.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module 2's bindings. Kept in its own file rather than folded into [AuthModule] so that adding the
 * dashboard did not require editing an authentication file.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
