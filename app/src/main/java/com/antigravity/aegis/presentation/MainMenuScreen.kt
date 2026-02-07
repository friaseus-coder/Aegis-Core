package com.antigravity.aegis.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.aegis.R
import com.antigravity.aegis.presentation.components.UserHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    mainViewModel: MainViewModel,
    onNavigateToCrm: () -> Unit,
    onNavigateToFieldService: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToMileage: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val enabledModules by mainViewModel.enabledModules.collectAsState()
    val userConfig by mainViewModel.userConfig.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.main_menu_title)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // User Header
            UserHeader(
                userConfig = userConfig,
                onNavigateToProfile = onNavigateToProfile,
                onToggleRole = { newRole ->
                    mainViewModel.toggleRole(newRole)
                }
            )

            Text(
                stringResource(R.string.main_menu_select_module),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (module in enabledModules) {
                    when (module) {
                        AppModule.CRM -> ModuleCard(
                            title = stringResource(R.string.home_module_crm_title),
                            subtitle = stringResource(R.string.home_module_crm_subtitle),
                            icon = Icons.Default.Home,
                            onClick = onNavigateToCrm
                        )
                        AppModule.FIELD_SERVICE -> ModuleCard(
                            title = stringResource(R.string.home_module_field_title),
                            subtitle = stringResource(R.string.home_module_field_subtitle),
                            icon = Icons.Default.Build,
                            onClick = onNavigateToFieldService
                        )
                        AppModule.BUDGETS -> ModuleCard(
                            title = stringResource(R.string.home_module_budgets_title),
                            subtitle = stringResource(R.string.home_module_budgets_subtitle),
                            icon = Icons.Default.DateRange,
                            onClick = onNavigateToBudgets
                        )
                        AppModule.EXPENSES -> ModuleCard(
                            title = stringResource(R.string.home_module_expenses_title),
                            subtitle = stringResource(R.string.home_module_expenses_subtitle),
                            icon = Icons.Default.Receipt,
                            onClick = onNavigateToExpenses
                        )
                        AppModule.INVENTORY -> ModuleCard(
                            title = stringResource(R.string.home_module_inventory_title),
                            subtitle = stringResource(R.string.home_module_inventory_subtitle),
                            icon = Icons.Default.ShoppingBag,
                            onClick = onNavigateToInventory
                        )
                        AppModule.MILEAGE -> ModuleCard(
                            title = stringResource(R.string.home_module_mileage_title),
                            subtitle = stringResource(R.string.home_module_mileage_subtitle),
                            icon = Icons.Default.DirectionsCar,
                            onClick = onNavigateToMileage
                        )
                    }
                }
            }
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
            .height(120.dp)
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
