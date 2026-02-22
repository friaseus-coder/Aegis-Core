package com.antigravity.aegis.domain.model

object CrmStatus {
    const val DRAFT = "Draft"
    const val SENT = "Sent"
    const val WON = "Won"
    const val LOST = "Lost"
    const val ARCHIVED = "Archived"
    
    // Antiguos estados de proyecto para compatibilidad o filtrado interno temporal
    const val ACTIVE = "Active"
    const val CLOSED = "Closed"

    // Helper method para asegurar que un valor recae en este pool
    fun isValid(status: String): Boolean {
        return listOf(DRAFT, SENT, WON, LOST, ARCHIVED, ACTIVE, CLOSED).contains(status)
    }
}
