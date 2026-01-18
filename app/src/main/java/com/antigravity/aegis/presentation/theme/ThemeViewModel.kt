package com.antigravity.aegis.presentation.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.antigravity.aegis.data.preferences.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreference: ThemePreference
) : ViewModel() {

    var isDarkTheme by mutableStateOf(themePreference.isDarkMode)
        private set

    fun toggleTheme() {
        val newMode = !isDarkTheme
        isDarkTheme = newMode
        themePreference.isDarkMode = newMode
    }
}
