package com.antigravity.aegis.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.antigravity.aegis.presentation.dashboard.DashboardScreen
import com.antigravity.aegis.presentation.screens.*

@Composable
fun NavigationGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    // Shared ViewModel for CRM domain
    val crmViewModel: com.antigravity.aegis.presentation.crm.CrmViewModel = androidx.hilt.navigation.compose.hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            com.antigravity.aegis.presentation.auth.LoginScreen(
                onLogin = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
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
            com.antigravity.aegis.presentation.crm.ClientListScreen(
                viewModel = crmViewModel,
                onNavigateToClientDetail = { clientId ->
                    crmViewModel.selectClient(clientId)
                    navController.navigate(Screen.ClientDetail.route)
                }
            )
        }

        composable(Screen.ClientDetail.route) {
            com.antigravity.aegis.presentation.crm.ClientDetailScreen(
                viewModel = crmViewModel,
                onNavigateToProject = { projectId ->
                    crmViewModel.selectProject(projectId)
                    navController.navigate(Screen.ProjectDetail.route)
                }
            )
        }

        composable(Screen.ProjectDetail.route) {
            com.antigravity.aegis.presentation.crm.ProjectDetailScreen(
                viewModel = crmViewModel,
                onNavigateToCreateReport = { projectId ->
                    // crmViewModel.selectProject(projectId) // Already selected
                    navController.navigate(Screen.CreateReport.createRoute(projectId))
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

        composable(Screen.CreateQuote.route) {
            com.antigravity.aegis.presentation.crm.CreateQuoteScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- OTHER MODULES ---

        composable(Screen.WorkReports.route) {
            com.antigravity.aegis.presentation.reports.FieldServiceScreen(
                viewModel = crmViewModel
            )
        }

        composable(Screen.Budgets.route) {
             com.antigravity.aegis.presentation.crm.QuoteKanbanScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateQuote = { navController.navigate(Screen.CreateQuote.route) }
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
        
        // --- PLACEHOLDERS ---
        composable(Screen.TimeControl.route) { TimeControlScreen() }
        composable(Screen.PasswordVault.route) { PasswordVaultScreen() }
    }
}
