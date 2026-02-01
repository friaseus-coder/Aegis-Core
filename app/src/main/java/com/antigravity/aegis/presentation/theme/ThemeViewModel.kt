package com.antigravity.aegis.presentation.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.map
import com.antigravity.aegis.data.preferences.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settingsRepository: com.antigravity.aegis.domain.repository.SettingsRepository
) : ViewModel() {

    // Ideally we subscribe to the flow
    // For Compose, we expose a State
    // However, MainActivity reads this on onCreate and setContent.
    // We should expose a Flow or State that MainActivity collects.
    
    val themeMode = settingsRepository.getUserConfig().map { 
        it?.themeMode ?: "dark"
    }

    val language = settingsRepository.getUserConfig().map { 
        it?.language ?: "es" // Default to Spanish
    }
    
    val companyLogoUri = settingsRepository.getUserConfig().map {
        it?.companyLogoUri
    }

    // Deprecated simple toggle, now we use SettingsScreen
    fun toggleTheme() {
        // No-op or cycle? Let's leave empty or implement cycling if needed.
        // For now, SettingsScreen handles the update.
    }
}
