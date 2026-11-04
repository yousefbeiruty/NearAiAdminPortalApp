package com.nearai.admin.navigation

import kotlinx.serialization.Serializable

/**
 * Serializable navigation routes for type-safe navigation.
 */

@Serializable
object OverviewRoute

@Serializable
object UsersRoute

@Serializable
object AiUsageRoute

@Serializable
object DiscoveryRoute

@Serializable
object MoreRoute

@Serializable
object LoginRoute

@Serializable
object SettingsRoute

@Serializable
object InventoryRoute

@Serializable
object SystemHealthRoute

@Serializable
object AuditLogsRoute

@Serializable
data class ProductEditRoute(val productId: String? = null)

@Serializable
object OrdersRoute

@Serializable
data class OrderDetailsRoute(val orderId: String)

@Serializable
object CategoriesRoute

@Serializable
object ProductsRoute

@Serializable
data class ProductDetailsRoute(val productId: String)

@Serializable
object ProductCreateRoute
