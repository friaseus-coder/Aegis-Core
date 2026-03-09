package com.antigravity.aegis.domain.util

import java.util.Currency

object CurrencyUtils {
    /**
     * Devuelve el símbolo asociado al código de moneda proporcionado (ej. "EUR" -> "€", "USD" -> "$").
     * Si no se puede determinar o el código es inválido, devuelve el mismo código por defecto.
     */
    fun getCurrencySymbol(currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            currency.symbol
        } catch (e: IllegalArgumentException) {
            // Código no válido, devolvemos el mismo código
            currencyCode
        } catch (e: Exception) {
            currencyCode
        }
    }
}
