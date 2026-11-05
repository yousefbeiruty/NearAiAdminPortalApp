package com.nearai.admin.feature.aiusage.data

import com.nearai.admin.feature.aiusage.domain.AiUsageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [AiUsageRepository] to [AiUsageRepositoryImpl].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AiUsageModule {

    @Binds
    @Singleton
    abstract fun bindsAiUsageRepository(
        impl: AiUsageRepositoryImpl
    ): AiUsageRepository
}
