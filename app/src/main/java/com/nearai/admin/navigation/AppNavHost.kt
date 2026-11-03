package com.nearai.admin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.feature.auth.presentation.AdminSignInScreen
import com.nearai.admin.feature.dashboard.presentation.DashboardScreen
import com.nearai.admin.feature.more.presentation.MoreScreen
import com.nearai.admin.feature.aiusage.presentation.AiUsageScreen
import com.nearai.admin.feature.auditlogs.presentation.AuditLogsScreen
import com.nearai.admin.feature.discovery.presentation.DiscoveryScreen
import com.nearai.admin.feature.inventory.presentation.InventoryScreen
import com.nearai.admin.feature.inventory.presentation.ProductEditScreen
import com.nearai.admin.feature.orders.presentation.OrderDetailsScreen
import com.nearai.admin.feature.orders.presentation.OrdersListScreen
import com.nearai.admin.feature.settings.presentation.SettingsScreen
import com.nearai.admin.feature.systemhealth.presentation.SystemHealthScreen
import com.nearai.admin.feature.users.presentation.UsersScreen

/**
 * Root navigation graph. Routes between authentication and main app screens
 * based on the current session state.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier,
    startDestination: Any = LoginRoute
) {
    val sessionState by sessionManager.sessionState.collectAsStateWithLifecycle()

    // Observe sessionState changes and sync navigation accordingly
    LaunchedEffect(sessionState) {
        val currentDestinationRoute = navController.currentDestination?.route
        when (sessionState) {
            is com.nearai.admin.core.security.SessionState.Authenticated -> {
                // If user is authenticated and currently on login screen, navigate to overview
                val isLoginScreen = currentDestinationRoute == null ||
                        currentDestinationRoute.endsWith("LoginRoute") ||
                        currentDestinationRoute.contains("LoginRoute")
                if (isLoginScreen) {
                    navController.navigate(OverviewRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                }
            }
            is com.nearai.admin.core.security.SessionState.SessionExpired,
            is com.nearai.admin.core.security.SessionState.NotAdmin,
            is com.nearai.admin.core.security.SessionState.Unauthenticated -> {
                // If user is unauthenticated/expired and not on login screen, navigate to login
                val isNotLoginScreen = currentDestinationRoute != null &&
                        !currentDestinationRoute.endsWith("LoginRoute") &&
                        !currentDestinationRoute.contains("LoginRoute")
                if (isNotLoginScreen) {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // Authentication
        composable<LoginRoute> {
            AdminSignInScreen(
                onSignInSuccess = {
                    navController.navigate(OverviewRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                }
            )
        }

        // Main app destinations
        composable<OverviewRoute> {
            DashboardScreen(
                onNavigateToDetails = { /* TODO */ },
                onNavigateToSystemHealth = {
                    navController.navigate(SystemHealthRoute)
                },
                onNavigateToAuditLogs = {
                    navController.navigate(AuditLogsRoute)
                }
            )
        }

        composable<UsersRoute> {
            UsersScreen()
        }

        composable<AiUsageRoute> {
            AiUsageScreen()
        }

        composable<DiscoveryRoute> {
            DiscoveryScreen()
        }

        composable<MoreRoute> {
            MoreScreen(
                onNavigateToOrders = {
                    navController.navigate(OrdersRoute)
                },
                onNavigateToInventory = {
                    navController.navigate(InventoryRoute)
                },
                onNavigateToSystemHealth = {
                    navController.navigate(SystemHealthRoute)
                },
                onNavigateToAuditLogs = {
                    navController.navigate(AuditLogsRoute)
                },
                onNavigateToSettings = {
                    navController.navigate(SettingsRoute)
                }
            )
        }

        // Orders
        composable<OrdersRoute> {
            OrdersListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOrder = { orderId ->
                    navController.navigate(OrderDetailsRoute(orderId))
                }
            )
        }

        // Order Details
        composable<OrderDetailsRoute> { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Sub-destinations under More
        composable<InventoryRoute> {
            InventoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddSku = { navController.navigate(ProductEditRoute) },
                onEditSku = { _ -> navController.navigate(ProductEditRoute) }
            )
        }

        composable<SystemHealthRoute> {
            SystemHealthScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AuditLogsRoute> {
            AuditLogsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    sessionManager.logout()
                    navController.navigate(LoginRoute) {
                        popUpTo<OverviewRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<ProductEditRoute> {
            ProductEditScreen(
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
