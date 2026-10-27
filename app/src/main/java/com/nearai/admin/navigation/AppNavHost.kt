package com.nearai.admin.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.feature.auth.presentation.AdminSignInScreen
import com.nearai.admin.feature.dashboard.presentation.DashboardScreen
import com.nearai.admin.feature.more.presentation.MoreScreen
import com.nearai.admin.feature.aiusage.presentation.AiUsageScreen
import com.nearai.admin.feature.discovery.presentation.DiscoveryScreen
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
    val sessionState = sessionManager.sessionState.collectAsStateWithLifecycle()

    // If session expires while in the app, navigate to login
    LaunchedEffect(sessionState.value) {
        if (sessionState.value is com.nearai.admin.core.security.SessionState.SessionExpired) {
            navController.navigate(LoginRoute) {
                popUpTo<OverviewRoute> { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
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
                onNavigateToDetails = { /* TODO */ }
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

        // Sub-destinations under More
        composable<InventoryRoute> {
            com.nearai.admin.feature.inventory.presentation.InventoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SystemHealthRoute> {
            com.nearai.admin.feature.systemhealth.presentation.SystemHealthScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AuditLogsRoute> {
            com.nearai.admin.feature.auditlogs.presentation.AuditLogsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SettingsRoute> {
            com.nearai.admin.feature.settings.presentation.SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    sessionManager.logout()
                    navController.navigate(LoginRoute) {
                        popUpTo<OverviewRoute> { inclusive = true }
                    }
                }
            )
        }
    }
}
