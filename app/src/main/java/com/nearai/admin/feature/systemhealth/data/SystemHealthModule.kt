package com.nearai.admin.feature.systemhealth.data

import com.nearai.admin.feature.systemhealth.domain.SystemHealthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for the System Health feature.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SystemHealthModule {

    @Binds
    @Singleton
    abstract fun bindsSystemHealthRepository(
        impl: SystemHealthRepositoryImpl
    ): SystemHealthRepository

    companion object {

        @Provides
        @Singleton
        fun provideSystemHealthApiService(
            retrofit: retrofit2.Retrofit
        ): SystemHealthApiService = retrofit.create(SystemHealthApiService::class.java)
    }
}
