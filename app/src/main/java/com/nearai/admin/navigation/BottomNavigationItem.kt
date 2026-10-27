package com.nearai.admin.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom navigation item definition with selected/unselected icons and label.
 */
data class BottomNavigationItem(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val primaryBottomNavigationItems = listOf(
    BottomNavigationItem(
        route = OverviewRoute,
        label = "Overview",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    ),
    BottomNavigationItem(
        route = UsersRoute,
        label = "Users",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    ),
    BottomNavigationItem(
        route = AiUsageRoute,
        label = "AI Usage",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    ),
    BottomNavigationItem(
        route = DiscoveryRoute,
        label = "Discovery",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore
    ),
    BottomNavigationItem(
        route = MoreRoute,
        label = "More",
        selectedIcon = Icons.Filled.Menu,
        unselectedIcon = Icons.Outlined.Menu
    )
)
