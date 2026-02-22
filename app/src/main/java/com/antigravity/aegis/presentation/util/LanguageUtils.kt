package com.antigravity.aegis.presentation.util

import java.util.Locale

object LanguageUtils {
    /**
     * Gets the default device language if it's supported (es, en, ca).
     * Falls back to "en" if the device language is not supported.
     */
    fun getDefaultPlatformLanguage(): String {
        val systemLang = Locale.getDefault().language
        return when (systemLang) {
            "es", "ca", "en" -> systemLang
            else -> "en" // Default to English for unsupported languages
        }
    }
}
