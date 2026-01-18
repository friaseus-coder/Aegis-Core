package com.antigravity.aegis

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.antigravity.aegis.presentation.navigation.NavigationGraph
import com.antigravity.aegis.ui.theme.AegisTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.activity.viewModels
import com.antigravity.aegis.presentation.theme.ThemeViewModel

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme = themeViewModel.isDarkTheme
            
            AegisTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavigationGraph(
                        navController = navController,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { themeViewModel.toggleTheme() }
                    )
                }
            }
        }
    }
}
