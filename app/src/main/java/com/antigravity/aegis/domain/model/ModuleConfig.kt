package com.antigravity.aegis.domain.model

/**
 * Modelo de dominio para la configuración de un módulo del dashboard
 * @param id Identificador único del módulo (ej: "projects", "clients", etc.)
 * @param nameResId ID del recurso string para el nombre del módulo
 * @param isVisible Si el módulo está visible en el dashboard
 * @param order Orden de visualización (menor número = más arriba)
 */
data class ModuleConfig(
    val id: String,
    val nameResId: Int,
    val isVisible: Boolean = true,
    val order: Int = 0
)
