package com.nearai.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.core.security.SessionState
import com.nearai.admin.core.security.TokenStorage
import com.nearai.admin.navigation.AppNavHost
import com.nearai.admin.navigation.NearAiBottomNavBar
import com.nearai.admin.navigation.OverviewRoute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var tokenStorage: TokenStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Determine start destination before composing UI to avoid flashing the login screen
        // when a valid session already exists. This check is fast (reads from encrypted prefs)
        // and does not block the main thread for long.
        val hasValidSession = tokenStorage.hasValidAdminSession()
        val startDestination = if (hasValidSession) {
            OverviewRoute
        } else {
            // No valid session stored; start at login.
            com.nearai.admin.navigation.LoginRoute
        }

        // Restore session asynchronously off the main thread to prevent ANR.
        // If a valid session exists, this updates sessionState to Authenticated,
        // which triggers AppNavHost's LaunchedEffect to navigate away from login.
        lifecycleScope.launch {
            sessionManager.restoreSession()
        }

        setContent {
            NearAiAdminTheme {
                val navController = rememberNavController()

                val sessionState by sessionManager.sessionState.collectAsStateWithLifecycle()
                val isAuthScreen = sessionState !is SessionState.Authenticated

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (!isAuthScreen) {
                            NearAiBottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        sessionManager = sessionManager,
                        startDestination = startDestination,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    )
                }
            }
        }
    }
}