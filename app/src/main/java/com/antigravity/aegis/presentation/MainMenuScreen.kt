package com.antigravity.aegis.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.antigravity.aegis.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onNavigateToCrm: () -> Unit,
    onNavigateToFieldService: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToMileage: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.main_menu_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
             verticalArrangement = Arrangement.spacedBy(16.dp) 
        ) {
            Text(
                stringResource(R.string.select_module),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Card 1: Project Hub
            ModuleCard(
                title = stringResource(R.string.module_crm_title),
                subtitle = stringResource(R.string.module_crm_subtitle),
                icon = androidx.compose.material.icons.Icons.Default.Home,
                onClick = onNavigateToCrm
            )

            // Card 2: Field Service
            ModuleCard(
                title = stringResource(R.string.module_field_title),
                subtitle = stringResource(R.string.module_field_subtitle),
                icon = androidx.compose.material.icons.Icons.Default.Build,
                onClick = onNavigateToFieldService
            )

            // Card 3: Budgets (Kanban)
            ModuleCard(
                title = stringResource(R.string.module_budgets_title),
                subtitle = stringResource(R.string.module_budgets_subtitle),
                icon = androidx.compose.material.icons.Icons.Default.DateRange,
                onClick = onNavigateToBudgets
            )

            // Card 4: Expenses (OCR)
            ModuleCard(
                title = stringResource(R.string.module_expenses_title),
                subtitle = stringResource(R.string.module_expenses_subtitle),
                icon = androidx.compose.material.icons.Icons.Default.Receipt,
                onClick = onNavigateToExpenses
            )

            // Card 5: Inventory (Scanner)
            ModuleCard(
                title = stringResource(R.string.module_inventory_title),
                subtitle = stringResource(R.string.module_inventory_subtitle),
                icon = androidx.compose.material.icons.Icons.Default.ShoppingBag,
                onClick = onNavigateToInventory
            )

            // Card 6: Mileage Log
            ModuleCard(
                title = stringResource(R.string.module_mileage_title),
                subtitle = stringResource(R.string.module_mileage_subtitle),
                icon = androidx.compose.material.icons.Icons.Default.DirectionsCar,
                onClick = onNavigateToMileage
            )
        }
    }
}

@Composable
fun ModuleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Reduced height as we don't have bg image
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
