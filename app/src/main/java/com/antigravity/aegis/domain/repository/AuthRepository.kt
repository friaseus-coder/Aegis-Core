package com.antigravity.aegis.domain.repository

interface AuthRepository {
    fun isSetupDone(): Boolean
    fun markSetupComplete()
}
