package com.nearai.admin.feature.auth.data

import com.nearai.admin.feature.auth.domain.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [AuthRepository] to [FakeAuthRepository].
 *
 * ⚠️ Currently bound to the development-only fake implementation.
 * When the backend API is ready, replace [FakeAuthRepository] with
 * [RealAuthRepository] (or whichever real implementation is created).
 */
@Module
@InstallIn(SingletonComponent::class)
interface AuthModule {

    @Binds
    @Singleton
    fun bindsAuthRepository(
        fakeAuthRepository: FakeAuthRepository
    ): AuthRepository
}
