package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.datasource.SecurityDataSource
import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val securityDataSource: SecurityDataSource
) : AuthRepository {

    override fun isSetupDone(): Boolean {
        return securityDataSource.isSetupDone()
    }

    override fun markSetupComplete() {
        securityDataSource.setSetupDone(true)
    }
}

