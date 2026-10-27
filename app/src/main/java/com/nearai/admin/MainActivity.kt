package com.nearai.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.core.security.SessionState
import com.nearai.admin.navigation.AppNavHost
import com.nearai.admin.navigation.primaryBottomNavigationItems
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Restore session on startup
        sessionManager.restoreSession()

        setContent {
            NearAiAdminTheme {
                val navController = rememberNavController()

                val sessionState by sessionManager.sessionState.collectAsStateWithLifecycle()
                val isAuthScreen = sessionState !is SessionState.Authenticated

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (!isAuthScreen) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            NearAiBottomNavigationBar(
                                items = primaryBottomNavigationItems,
                                currentRoute = currentRoute,
                                onNavigateTo = { route ->
                                    navController.navigate(route) {
                                        // Pop back to the start destination to avoid accumulating screens
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Restore state when re-selecting a tab
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        sessionManager = sessionManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun NearAiBottomNavigationBar(
    items: List<com.nearai.admin.navigation.BottomNavigationItem>,
    currentRoute: String?,
    onNavigateTo: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item::class.java.name
                || currentRoute?.contains(item.label.lowercase()) == true

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigateTo(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(item.label)
                }
            )
        }
    }
}