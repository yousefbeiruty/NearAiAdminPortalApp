package com.nearai.admin.feature.auditlogs.data

import com.nearai.admin.feature.auditlogs.domain.AuditLogsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for the Audit Logs feature.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuditLogsModule {

    @Binds
    @Singleton
    abstract fun bindsAuditLogsRepository(
        impl: AuditLogsRepositoryImpl
    ): AuditLogsRepository

    companion object {

        @Provides
        @Singleton
        fun provideAuditLogsApiService(
            retrofit: retrofit2.Retrofit
        ): AuditLogsApiService = retrofit.create(AuditLogsApiService::class.java)
    }
}
