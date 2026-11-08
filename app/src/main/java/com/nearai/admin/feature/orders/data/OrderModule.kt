package com.nearai.admin.feature.orders.data

import com.nearai.admin.feature.orders.domain.OrderRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for the Orders feature.
 *
 * Provides [OrderApiService] via Retrofit and binds [OrderRepository] to [OrderRepositoryImpl].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class OrderModule {

    @Binds
    @Singleton
    abstract fun bindsOrderRepository(
        impl: OrderRepositoryImpl
    ): OrderRepository

    companion object {

        @Provides
        @Singleton
        fun provideOrderApiService(
            retrofit: retrofit2.Retrofit
        ): OrderApiService = retrofit.create(OrderApiService::class.java)

        @Provides
        @Singleton
        fun providePaymentApiService(
            retrofit: retrofit2.Retrofit
        ): PaymentApiService = retrofit.create(PaymentApiService::class.java)
    }
}
