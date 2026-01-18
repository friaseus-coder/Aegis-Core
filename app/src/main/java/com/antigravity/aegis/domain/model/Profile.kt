package com.antigravity.aegis.domain.model

data class Profile(
    val id: String,
    val name: String,
    val avatarUrl: String? = null, // In local, this could be a resource ID or local file path
    val language: String = "es" // "es" or "en"
)
