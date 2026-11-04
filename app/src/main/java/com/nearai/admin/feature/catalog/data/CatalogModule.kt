package com.nearai.admin.feature.catalog.data

import com.nearai.admin.feature.catalog.domain.CatalogRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for the Catalog feature.
 *
 * Provides [CatalogApiService] via Retrofit and binds [CatalogRepository] to [CatalogRepositoryImpl].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CatalogModule {

    @Binds
    @Singleton
    abstract fun bindsCatalogRepository(
        impl: CatalogRepositoryImpl
    ): CatalogRepository

    companion object {

        @Provides
        @Singleton
        fun provideCatalogApiService(
            retrofit: retrofit2.Retrofit
        ): CatalogApiService = retrofit.create(CatalogApiService::class.java)
    }
}
