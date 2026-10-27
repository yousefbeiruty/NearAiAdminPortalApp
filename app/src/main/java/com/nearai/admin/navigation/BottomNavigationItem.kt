package com.nearai.admin.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme

// =============================================================================
// Bottom Navigation Destination Routes & Definitions
// =============================================================================

sealed class AdminBottomDestination(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null,
    val hasAlertDot: Boolean = false
) {
    object Overview : AdminBottomDestination(
        route = OverviewRoute,
        label = "Overview",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    object Users : AdminBottomDestination(
        route = UsersRoute,
        label = "Users",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    )

    object AIUsage : AdminBottomDestination(
        route = AiUsageRoute,
        label = "AI Compute",
        selectedIcon = Icons.Filled.Memory,
        unselectedIcon = Icons.Outlined.Memory,
        hasAlertDot = true // Shows soft-cap warning indicator (71% budget consumed)
    )

    object Places : AdminBottomDestination(
        route = DiscoveryRoute,
        label = "Discovery",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore
    )

    object More : AdminBottomDestination(
        route = MoreRoute,
        label = "More",
        selectedIcon = Icons.Filled.MoreHoriz,
        unselectedIcon = Icons.Outlined.MoreHoriz
    )

    companion object {
        val items = listOf(
            Overview,
            Users,
            AIUsage,
            Places,
            More
        )
    }
}

// =============================================================================
// NearAI Admin Bottom Navigation Bar Composable (Stateful — NavController)
// =============================================================================

@Composable
fun NearAiBottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderSubtle),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        AdminBottomDestination.items.forEach { screen ->
            // The route property is a @Serializable object. We compare by checking
            // whether the current destination route string matches the simple name
            // of the route object class (e.g. "OverviewRoute", "UsersRoute", etc.)
            val routeSimpleName = screen.route::class.java.simpleName
            val isSelected = currentRoute?.endsWith(routeSimpleName) == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(screen.route) {
                            // Pop up to Overview (first main tab) to avoid accumulating screens,
                            // without popping back to LoginRoute which is the graph start destination
                            popUpTo<OverviewRoute> {
                                saveState = true
                            }
                            // Avoid multiple copies of same destination
                            launchSingleTop = true
                            // Restore state when re-selecting a tab
                            restoreState = true
                        }
                    }
                },
                icon = {
                    BadgedBox(
                        badge = {
                            when {
                                screen.badgeCount != null -> {
                                    Badge(
                                        containerColor = RoseError,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = screen.badgeCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                screen.hasAlertDot -> {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Indigo)
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    // Active tab styling
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    // Inactive tab styling
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// =============================================================================
// Stateless Variant (For Easy Custom State / Scaffold Injection)
// =============================================================================

@Composable
fun NearAiBottomNavBarStateless(
    currentRoute: String,
    onTabSelected: (AdminBottomDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderSubtle),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        AdminBottomDestination.items.forEach { screen ->
            val isSelected = currentRoute == screen::class.java.simpleName

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(screen) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (screen.hasAlertDot) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Indigo)
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true, widthDp = 390)
@Composable
fun NearAiBottomNavBarPreview() {
    NearAiAdminTheme {
        NearAiBottomNavBarStateless(
            currentRoute = AdminBottomDestination.Overview::class.java.simpleName,
            onTabSelected = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun NearAiBottomNavBarAiTabActivePreview() {
    NearAiAdminTheme {
        NearAiBottomNavBarStateless(
            currentRoute = AdminBottomDestination.AIUsage::class.java.simpleName,
            onTabSelected = {}
        )
    }
}
