package com.antigravity.aegis.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object CreateUser : Screen("create_user")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Projects : Screen("projects")
    data object WorkReports : Screen("work_reports")
    data object Budgets : Screen("budgets")
    data object Expenses : Screen("expenses")
    data object Inventory : Screen("inventory")
    data object TimeControl : Screen("time_control")
    data object PasswordVault : Screen("password_vault")
    data object Mileage : Screen("mileage")
    data object Settings : Screen("settings")

    // CRM Sub-routes
    data object Clients : Screen("clients")
    data object ClientDetail : Screen("client_detail")
    data object ProjectDetail : Screen("project_detail")
    data object CreateReport : Screen("create_report/{projectId}") {
        fun createRoute(projectId: Int) = "create_report/$projectId"
    }
    data object ClientEdit : Screen("client_edit/{clientId}") {
        fun createRoute(clientId: Int) = "client_edit/$clientId"
    }
    data object CreateQuote : Screen("create_quote")
}
