package com.antigravity.aegis.domain.model

data class Client(
    val id: Int = 0,
    val firstName: String,
    val lastName: String = "",
    val tipoCliente: ClientType = ClientType.PARTICULAR,
    val razonSocial: String? = null,
    val nifCif: String? = null,
    val personaContacto: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: Address? = null,
    val categoria: ClientCategory = ClientCategory.POTENTIAL,
    val notas: String? = null
)

enum class ClientType {
    PARTICULAR, EMPRESA
}

enum class ClientCategory {
    POTENTIAL, ACTIVE, INACTIVE
}

data class Address(
    val calle: String? = null,
    val numero: String? = null,
    val piso: String? = null,
    val poblacion: String? = null,
    val codigoPostal: String? = null
)
