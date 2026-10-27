package com.nearai.admin.feature.dashboard.data

import com.nearai.admin.feature.dashboard.domain.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [DashboardRepository] to [FakeDashboardRepository].
 *
 * ⚠️ Currently bound to the development-only fake implementation.
 * Replace with a real implementation when the backend API is connected.
 */
@Module
@InstallIn(SingletonComponent::class)
interface DashboardModule {

    @Binds
    @Singleton
    fun bindsDashboardRepository(
        fakeDashboardRepository: FakeDashboardRepository
    ): DashboardRepository
}
