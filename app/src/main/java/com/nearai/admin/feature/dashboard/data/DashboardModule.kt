package com.nearai.admin.feature.dashboard.data

import com.nearai.admin.feature.dashboard.domain.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [DashboardRepository] to [DashboardRepositoryImpl].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    @Singleton
    abstract fun bindsDashboardRepository(
        impl: DashboardRepositoryImpl
    ): DashboardRepository

    companion object {

        @Provides
        @Singleton
        fun provideDashboardApiService(
            retrofit: retrofit2.Retrofit
        ): DashboardApiService = retrofit.create(DashboardApiService::class.java)
    }
}
