package com.antigravity.aegis.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PlaceholderScreen(moduleName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$moduleName\n(Módulo en Construcción)",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ProjectsScreen() = PlaceholderScreen("Proyectos")

@Composable
fun WorkReportsScreen() = PlaceholderScreen("Partes de Trabajo")

@Composable
fun BudgetsScreen() = PlaceholderScreen("Presupuestos")

@Composable
fun ExpensesScreen() = PlaceholderScreen("Gastos")

@Composable
fun InventoryScreen() = PlaceholderScreen("Inventario")



@Composable
fun PasswordVaultScreen() = PlaceholderScreen("Password Vault")

@Composable
fun MileageScreen() = PlaceholderScreen("Kilometraje")
