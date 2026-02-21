package com.antigravity.aegis.data.model

import com.antigravity.aegis.data.local.entity.*

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val userConfig: UserConfig? = null,
    val clients: List<ClientEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val quotes: List<QuoteEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val mileageLogs: List<MileageLogEntity> = emptyList()
)
