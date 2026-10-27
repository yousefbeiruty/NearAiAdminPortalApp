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
import androidx.navigation.compose.rememberNavController
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.core.security.SessionState
import com.nearai.admin.navigation.AppNavHost
import com.nearai.admin.navigation.NearAiBottomNavBar
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
                            NearAiBottomNavBar(navController = navController)
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