package com.nearai.admin.feature.discovery.data

import com.nearai.admin.feature.discovery.domain.DiscoveryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [DiscoveryRepository] to [DiscoveryRepositoryImpl].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DiscoveryModule {

    @Binds
    @Singleton
    abstract fun bindsDiscoveryRepository(
        impl: DiscoveryRepositoryImpl
    ): DiscoveryRepository

    companion object {

        @Provides
        @Singleton
        fun provideDiscoveryApiService(
            retrofit: retrofit2.Retrofit
        ): DiscoveryApiService = retrofit.create(DiscoveryApiService::class.java)
    }
}
