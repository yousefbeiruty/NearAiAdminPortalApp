package com.nearai.admin.feature.discovery.data

import com.nearai.admin.feature.discovery.domain.BusinessAnalyticsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [BusinessAnalyticsRepository] to [BusinessAnalyticsRepositoryImpl].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BusinessAnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindsBusinessAnalyticsRepository(
        impl: BusinessAnalyticsRepositoryImpl
    ): BusinessAnalyticsRepository

    companion object {

        @Provides
        @Singleton
        fun provideBusinessAnalyticsApiService(
            retrofit: retrofit2.Retrofit
        ): BusinessAnalyticsApiService = retrofit.create(BusinessAnalyticsApiService::class.java)
    }
}
