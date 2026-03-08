package com.antigravity.aegis.presentation.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.antigravity.aegis.presentation.auth.*
import com.antigravity.aegis.presentation.crm.*
import com.antigravity.aegis.presentation.screens.PasswordVaultScreen
import com.antigravity.aegis.presentation.dashboard.DashboardScreen
import com.antigravity.aegis.presentation.expenses.ExpensesScreen
import com.antigravity.aegis.presentation.inventory.InventoryScreen
import com.antigravity.aegis.presentation.mileage.MileageScreen
import com.antigravity.aegis.presentation.settings.*
import com.antigravity.aegis.presentation.feature.clients.ClientListScreen
import com.antigravity.aegis.presentation.timecontrol.TimeControlScreen
import com.antigravity.aegis.presentation.backup.ImportBackupScreen

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    navigation(
        startDestination = Screen.Login.route,
        route = Screen.AuthGraph.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToCreateUser = {
                    navController.navigate(Screen.CreateUser.route)
                },
                onNavigateToImport = {
                    navController.navigate(Screen.ImportBackup.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.AuthGraph.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateUser.route) {
            val authState by authViewModel.authState.collectAsState()
            val setupState by authViewModel.setupState.collectAsState()

            if (authState == AuthState.NeedsSetup && setupState != null) {
                SetupScreen(
                    state = setupState!!,
                    onConfirm = { language ->
                        authViewModel.setLanguage(language)
                        authViewModel.confirmSetup() // Inicia el OS lock
                    }
                )
            } else if (authState == AuthState.Authenticated) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.AuthGraph.route) { inclusive = true }
                    }
                }
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }




    }
}

fun NavGraphBuilder.crmGraph(
    navController: NavHostController,
    crmViewModel: CrmViewModel
) {
    navigation(
        startDestination = Screen.Projects.route,
        route = Screen.CrmGraph.route
    ) {
        composable(Screen.Projects.route) {
            com.antigravity.aegis.presentation.crm.DashboardScreen(
                viewModel = crmViewModel,
                onNavigateToClients = { navController.navigate(Screen.Clients.route) },
                onNavigateToProject = { projectId ->
                    navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                }
            )
        }

        composable(Screen.Clients.route) {
            ClientListScreen(
                crmViewModel = crmViewModel,
                onNavigateToClientDetail = { clientId ->
                    navController.navigate(Screen.ClientDetail.createRoute(clientId))
                },
                onNavigateToClientCreate = {
                    navController.navigate(Screen.ClientEdit.createRoute(0))
                }
            )
        }

        composable(Screen.ClientDetail.route) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId")?.toIntOrNull() ?: 0
            
            androidx.compose.runtime.LaunchedEffect(clientId) {
                if (clientId != 0) crmViewModel.selectClient(clientId)
            }

            com.antigravity.aegis.presentation.crm.ClientDashboardScreen(
                viewModel = crmViewModel,
                onNavigateToProject = { projectId ->
                    navController.navigate(Screen.ProjectDetail.createRoute(projectId))
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
            ClientEditScreen(
                viewModel = crmViewModel,
                clientId = if (clientId == 0) null else clientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProjectDetail.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull() ?: 0
            
            androidx.compose.runtime.LaunchedEffect(projectId) {
                if (projectId != 0) crmViewModel.selectProject(projectId)
            }

            ProjectDetailScreen(
                viewModel = crmViewModel,
                onNavigateToEditBudget = { pId, quoteId ->
                    navController.navigate(Screen.EditBudget.createRoute(pId, quoteId))
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
            BudgetEditorScreen(
                projectId = if (projectId == 0) null else projectId,
                quoteId = if (quoteId == 0) null else quoteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Budgets.route) {
            QuoteKanbanScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProject = { projectId ->
                    navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                },
                onNavigateToEditBudget = { quoteId ->
                    navController.navigate(Screen.EditBudget.createRoute(0, quoteId))
                }
            )
        }

        composable(Screen.Templates.route) {
            TemplateListScreen(
                viewModel = crmViewModel,
                onNavigateToDetail = { templateId ->
                    navController.navigate(Screen.ProjectDetail.createRoute(templateId))
                }
            )
        }

        composable(Screen.ArchivedProjects.route) {
            ArchivedProjectsScreen(
                viewModel = crmViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

fun NavGraphBuilder.settingsGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = Screen.Settings.route,
        route = Screen.SettingsGraph.route
    ) {
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackup = { navController.navigate(Screen.ImportBackup.route) },
                onNavigateToModuleCustomization = { navController.navigate(Screen.ModuleCustomization.route) },
                onNavigateToTemplates = { navController.navigate(Screen.Templates.route) },
                onLogout = {
                    navController.navigate(Screen.AuthGraph.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ModuleCustomization.route) {
            ModuleCustomizationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.ImportBackup.route) {
            ImportBackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

fun NavGraphBuilder.featuresGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = Screen.Dashboard.route, // This is tricky, maybe features shouldn't have a root like this
        route = Screen.FeaturesGraph.route
    ) {
        composable(Screen.Expenses.route) {
            ExpensesScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Mileage.route) {
            MileageScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.TimeControl.route) {
            TimeControlScreen()
        }

        composable(Screen.PasswordVault.route) {
            PasswordVaultScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
