package com.antigravity.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String, // Nombre (Particular) o Nombre Comercial (Empresa)
    val lastName: String = "", // Apellidos (Particular). Vacío para empresas.
    val tipoCliente: String = "Particular", // "Particular" o "Empresa"
    val razonSocial: String? = null, // Solo para Empresas
    val nifCif: String? = null,
    val personaContacto: String? = null, // Solo para Empresas
    val phone: String? = null,
    val email: String? = null,
    // Dirección desglosada
    val calle: String? = null,
    val numero: String? = null,
    val piso: String? = null,
    val poblacion: String? = null,
    val codigoPostal: String? = null,
    // Categoría auto-calculada (Activo si tiene proyectos/presupuestos, Potencial si no)
    val categoria: String = "Potencial",
    val notas: String? = null,
    val isSynced: Boolean = false
)

