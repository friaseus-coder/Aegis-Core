package com.antigravity.aegis

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.antigravity.aegis.presentation.navigation.NavigationGraph
import com.antigravity.aegis.ui.theme.AegisTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.map
import com.antigravity.aegis.presentation.theme.ThemeViewModel

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.antigravity.aegis.data.worker.CloudSyncWorker

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enqueue Cloud Sync Worker
        CloudSyncWorker.enqueue(this)
        
        // Collect Theme Mode
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState(initial = "dark")
            val language by themeViewModel.language.collectAsState(initial = null)

            // Apply Language (Side Effect)
            // Note: This might cause Activity recreation loop if not handled carefully.
            // setApplicationLocales saves to storage automatically in newer Android versions.
            // Since we persist in DB, we want to align them.
            // We use LaunchedEffect to trigger only on change.
            androidx.compose.runtime.LaunchedEffect(language) {
                 language?.let { lang ->
                     val currentLocales = AppCompatDelegate.getApplicationLocales()
                     val currentLang = if (!currentLocales.isEmpty) currentLocales.get(0)?.language else com.antigravity.aegis.presentation.util.LanguageUtils.getDefaultPlatformLanguage()
                     
                     if (currentLang != lang) {
                         val appLocale = LocaleListCompat.forLanguageTags(lang)
                         AppCompatDelegate.setApplicationLocales(appLocale)
                     }
                 }
            }
            
            val isDarkTheme = when(themeMode) {
                "dark" -> true
                "light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            // Note: Language change usually requires Activity restart or context wrapping.
            // For this implementation, we assume the user might need to restart app 
            // or we use AppCompatDelegate.setApplicationLocales inside an observer.
            
            val companyLogoUri by themeViewModel.companyLogoUri.collectAsState(initial = null)
            
            AegisTheme(darkTheme = isDarkTheme) {
                androidx.compose.runtime.CompositionLocalProvider(com.antigravity.aegis.ui.theme.LocalCompanyLogoUri provides companyLogoUri) {
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
}
