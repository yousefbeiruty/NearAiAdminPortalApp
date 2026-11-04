package com.nearai.admin.feature.users.data

import com.nearai.admin.feature.users.domain.UsersRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for the Users feature.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UsersModule {

    @Binds
    @Singleton
    abstract fun bindsUsersRepository(
        impl: UsersRepositoryImpl
    ): UsersRepository

    companion object {

        @Provides
        @Singleton
        fun provideUsersApiService(
            retrofit: retrofit2.Retrofit
        ): UsersApiService = retrofit.create(UsersApiService::class.java)
    }
}
