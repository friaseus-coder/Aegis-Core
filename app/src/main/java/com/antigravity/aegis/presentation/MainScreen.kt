package com.antigravity.aegis.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antigravity.aegis.presentation.auth.AuthState
import com.antigravity.aegis.presentation.auth.AuthViewModel
import com.antigravity.aegis.presentation.auth.LoginScreen
import com.antigravity.aegis.presentation.auth.SetupScreen

@Composable
fun MainScreen(
    viewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()
    val setupState by viewModel.setupState.collectAsState()

    // Simple state-based navigation for Auth
    when (authState) {
        AuthState.Loading -> {
            // Show Loading Indicator
        }
        AuthState.NeedsSetup -> {
            setupState?.let { state ->
                SetupScreen(
                    state = state,
                    onConfirm = { pin -> viewModel.confirmSetup(pin) }
                )
            }
        }
        AuthState.Locked -> {
            LoginScreen(
                onLogin = { pin -> viewModel.login(pin) }
            )
        }
        AuthState.Authenticated -> {
            // CRM / Project Hub Navigation
            val crmViewModel: com.antigravity.aegis.presentation.crm.CrmViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val crmNavController = rememberNavController()

            NavHost(navController = crmNavController, startDestination = "main_menu") {
                composable("main_menu") {
                    MainMenuScreen(
                        onNavigateToCrm = { crmNavController.navigate("dashboard") },
                        onNavigateToFieldService = { crmNavController.navigate("field_service") },
                        onNavigateToBudgets = { crmNavController.navigate("budgets") },
                        onNavigateToExpenses = { crmNavController.navigate("expenses") },
                        onNavigateToInventory = { crmNavController.navigate("inventory") },
                        onNavigateToMileage = { crmNavController.navigate("mileage") }
                    )
                }

                composable("field_service") {
                    com.antigravity.aegis.presentation.reports.FieldServiceScreen(
                        viewModel = crmViewModel
                    )
                }

                composable("dashboard") {
                    com.antigravity.aegis.presentation.crm.DashboardScreen(
                        viewModel = crmViewModel,
                        onNavigateToClients = { crmNavController.navigate("clients") },
                        onNavigateToProject = { projectId ->
                            crmViewModel.selectProject(projectId)
                            crmNavController.navigate("project_detail")
                        }
                    )
                }
                composable("clients") {
                    com.antigravity.aegis.presentation.crm.ClientListScreen(
                        viewModel = crmViewModel,
                        onNavigateToClientDetail = { clientId ->
                             crmViewModel.selectClient(clientId)
                             crmNavController.navigate("client_detail")
                        }
                    )
                }
                composable("client_detail") {
                    com.antigravity.aegis.presentation.crm.ClientDetailScreen(
                        viewModel = crmViewModel,
                        onNavigateToProject = { projectId ->
                            crmViewModel.selectProject(projectId)
                            crmNavController.navigate("project_detail")
                        }
                    )
                }
                composable("project_detail") {
                    com.antigravity.aegis.presentation.crm.ProjectDetailScreen(
                        viewModel = crmViewModel,
                        onNavigateToCreateReport = { projectId ->
                            crmViewModel.selectProject(projectId) // Ensure selected
                            crmNavController.navigate("create_report/$projectId")
                        }
                    )
                }
                composable("create_report/{projectId}") { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getString("projectId")?.toIntOrNull() ?: 0
                    com.antigravity.aegis.presentation.reports.CreateReportScreen(
                        projectId = projectId,
                        onSaveReport = { desc, sig ->
                            crmViewModel.createWorkReport(projectId, desc, sig)
                            crmNavController.popBackStack()
                        },
                        onNavigateBack = { crmNavController.popBackStack() }
                    )
                }
                
                composable("budgets") {
                    com.antigravity.aegis.presentation.crm.QuoteKanbanScreen(
                        onNavigateBack = { crmNavController.popBackStack() },
                        onNavigateToCreateQuote = { crmNavController.navigate("create_quote") }
                    )
                }

                composable("create_quote") {
                    com.antigravity.aegis.presentation.crm.CreateQuoteScreen(
                        onNavigateBack = { crmNavController.popBackStack() }
                    )
                }

                composable("expenses") {
                    com.antigravity.aegis.presentation.expenses.ExpensesScreen(
                        onNavigateBack = { crmNavController.popBackStack() }
                    )
                }

                composable("inventory") {
                    com.antigravity.aegis.presentation.inventory.InventoryScreen(
                        onNavigateBack = { crmNavController.popBackStack() }
                    )
                }

                composable("mileage") {
                    com.antigravity.aegis.presentation.mileage.MileageScreen(
                        onNavigateBack = { crmNavController.popBackStack() }
                    )
                }

                composable("settings") {
                    com.antigravity.aegis.presentation.settings.SettingsScreen(
                        onNavigateBack = { crmNavController.popBackStack() },
                        onLogout = { 
                            viewModel.logout() 
                        }
                    )
                }
            }
            
            // Global Settings Button Overlay
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize()
            ) {
               // This overlay captures clicks? No, checking logic.
               // We need the button to be on top.
               // We cannot wrap NavHost easily without blocking interaction if not careful.
               // But a small button in corner is fine.
            }
            // Better approach: Floating Action Button logic or just put it in the Box alongside NavHost
        }
    }
}
