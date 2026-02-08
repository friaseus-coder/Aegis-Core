package com.antigravity.aegis.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.antigravity.aegis.presentation.dashboard.DashboardScreen
import com.antigravity.aegis.presentation.screens.*
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
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            com.antigravity.aegis.presentation.auth.SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateUser.route) {
            val authState by authViewModel.authState.collectAsState()
            val setupState by authViewModel.setupState.collectAsState()

            if (authState == AuthState.NeedsSetup && setupState != null) {
                // Show Initial Setup (Seed Phrase + Profile Selection)
                SetupScreen(
                    state = setupState!!,
                    onConfirm = { name, language, pin, role, email, phone ->
                         authViewModel.confirmSetup(name, language, pin, role, email, phone)
                         // Navigate to Dashboard is handled by AuthViewModel state change?
                         // Actually confirmSetup changes state to Authenticated.
                         // But we might need to navigate manually if logic depends on it.
                         // If state becomes Authenticated, where do we go?
                         // We are in CreateUser route.
                         // If AuthState changes to Authenticated, LoginScreen (if we were there) would navigate.
                         // But here we are independent.
                         navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                         }
                    }
                )
            } else {
                // Show User Creation (Standard)
                com.antigravity.aegis.presentation.auth.CreateUserScreen(
                    onUserCreated = { name, lang, pin ->
                        authViewModel.createUser(name, lang, pin, null, null)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.CreateUser.route) { inclusive = true }
                            // Also clear backstack so they can't go back to login/create
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.Login.route) {
            com.antigravity.aegis.presentation.auth.LoginScreen(
                onLogin = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToCreateUser = {
                     navController.navigate(Screen.CreateUser.route)
                },
                onNavigateToRecovery = {
                    navController.navigate(Screen.Recovery.route)
                },
                onNavigateToImport = {
                    navController.navigate(Screen.ImportBackup.route)
                },
                onNavigateToChangePin = {
                    navController.navigate(Screen.ChangePin.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // --- CRM / PROJECTS MODULES ---
        
        // Projects Hub (CRM Dashboard)
        composable(Screen.Projects.route) {
            com.antigravity.aegis.presentation.crm.DashboardScreen(
                viewModel = crmViewModel,
                onNavigateToClients = { navController.navigate(Screen.Clients.route) },
                onNavigateToProject = { projectId ->
                    crmViewModel.selectProject(projectId)
                    navController.navigate(Screen.ProjectDetail.route)
                }
            )
        }

        composable(Screen.Clients.route) {
            com.antigravity.aegis.presentation.feature.clients.ClientListScreen(
                onNavigateToClientDetail = { clientId ->
                    crmViewModel.selectClient(clientId)
                    navController.navigate(Screen.ClientDetail.route)
                },
                onNavigateToClientCreate = {
                    navController.navigate(Screen.ClientEdit.createRoute(0))
                }
            )
        }

        composable(Screen.ClientDetail.route) {
            com.antigravity.aegis.presentation.crm.ClientDashboardScreen(
                viewModel = crmViewModel,
                onNavigateToProject = { projectId ->
                    crmViewModel.selectProject(projectId)
                    navController.navigate(Screen.ProjectDetail.route)
                },
                onEditClient = {
                    val client = crmViewModel.selectedClient.value
                    if (client != null) {
                        navController.navigate(Screen.ClientEdit.createRoute(client.id))
                    }
                }
            )
        }

        composable(Screen.ClientEdit.route) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId")?.toIntOrNull() ?: 0
            com.antigravity.aegis.presentation.crm.ClientEditScreen(
                viewModel = crmViewModel,
                clientId = if (clientId == 0) null else clientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProjectDetail.route) {
            com.antigravity.aegis.presentation.crm.ProjectDetailScreen(
                viewModel = crmViewModel,
                onNavigateToCreateReport = { projectId ->
                    // crmViewModel.selectProject(projectId) // Already selected
                    navController.navigate(Screen.CreateReport.createRoute(projectId))
                },
                onNavigateToEditBudget = { projectId, quoteId ->
                    navController.navigate(Screen.EditBudget.createRoute(projectId, quoteId))
                }
            )
        }

        composable(Screen.CreateReport.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull() ?: 0
            com.antigravity.aegis.presentation.reports.CreateReportScreen(
                projectId = projectId,
                onSaveReport = { desc, sig ->
                     crmViewModel.createWorkReport(projectId, desc, sig)
                     navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditBudget.route,
            arguments = listOf(
                androidx.navigation.navArgument("projectId") { type = androidx.navigation.NavType.IntType; defaultValue = 0 },
                androidx.navigation.navArgument("quoteId") { type = androidx.navigation.NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0
            val quoteId = backStackEntry.arguments?.getInt("quoteId") ?: 0
            com.antigravity.aegis.presentation.crm.BudgetEditorScreen(
                projectId = if (projectId == 0) null else projectId,
                quoteId = if (quoteId == 0) null else quoteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- OTHER MODULES ---

        composable(Screen.WorkReports.route) {
            com.antigravity.aegis.presentation.reports.FieldServiceScreen(
                viewModel = crmViewModel,
                onNavigateToCreateReport = { projectId ->
                    navController.navigate(Screen.CreateReport.createRoute(projectId))
                }
            )
        }

        composable(Screen.Budgets.route) {
             com.antigravity.aegis.presentation.crm.QuoteKanbanScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateQuote = { navController.navigate(Screen.EditBudget.createRoute(0, 0)) }
            )
        }

        composable(Screen.Expenses.route) {
            com.antigravity.aegis.presentation.expenses.ExpensesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Inventory.route) {
            com.antigravity.aegis.presentation.inventory.InventoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Mileage.route) {
             com.antigravity.aegis.presentation.mileage.MileageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // --- SETTINGS ---
        composable(Screen.Settings.route) {
            com.antigravity.aegis.presentation.settings.SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackup = { navController.navigate(Screen.ImportBackup.route) },
                onNavigateToModuleCustomization = { navController.navigate(Screen.ModuleCustomization.route) },
                onLogout = {
                    // Navigate back to Login and clear backstack
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ModuleCustomization.route) {
            com.antigravity.aegis.presentation.settings.ModuleCustomizationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // --- PLACEHOLDERS ---
        composable(Screen.TimeControl.route) { TimeControlScreen() }
        composable(Screen.PasswordVault.route) { PasswordVaultScreen() }
        
        // --- AUTH & UTILITIES ---
        composable(Screen.Recovery.route) {
            com.antigravity.aegis.presentation.auth.RecoveryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangePin = {
                    navController.navigate(Screen.ChangePin.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ChangePin.route) {
             com.antigravity.aegis.presentation.auth.ChangePinScreen(
                 onPinChanged = {
                     navController.navigate(Screen.Dashboard.route) {
                         popUpTo(Screen.Login.route) { inclusive = true }
                     }
                 }
             )
        }
        
        composable(Screen.ImportBackup.route) {
            com.antigravity.aegis.presentation.backup.ImportBackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
