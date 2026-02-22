package com.antigravity.aegis.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.antigravity.aegis.presentation.dashboard.DashboardScreen
import com.antigravity.aegis.presentation.screens.PasswordVaultScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.antigravity.aegis.presentation.auth.AuthState
import com.antigravity.aegis.presentation.auth.SetupScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    // Shared ViewModel for CRM domain
    val crmViewModel: com.antigravity.aegis.presentation.crm.CrmViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val authViewModel: com.antigravity.aegis.presentation.auth.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.AuthGraph.route
    ) {
        // --- AUTH GRAPH ---
        authGraph(navController, authViewModel)

        // --- DASHBOARD ---
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onNavigateToSettings = { navController.navigate(Screen.SettingsGraph.route) }
            )
        }

        // --- CRM GRAPH ---
        crmGraph(navController, crmViewModel)

        // --- SETTINGS GRAPH ---
        settingsGraph(navController)

        // --- FEATURES GRAPH ---
        featuresGraph(navController)

        // Splash (Extra-graph as it's the start destination of everything if needed)
        composable(Screen.Splash.route) {
            com.antigravity.aegis.presentation.auth.SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.AuthGraph.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
