package com.antigravity.aegis.domain.util

/**
 * Clase sellada que representa el resultado de una operación que puede fallar.
 * Es 100% Kotlin puro, sin dependencias de Android, para mantener la capa
 * Domain desacoplada del framework.
 *
 * Uso:
 *   when (result) {
 *       is Result.Success -> result.data
 *       is Result.Error   -> result.message
 *   }
 */
sealed class Result<out T> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(
        val exception: Throwable,
        val message: String? = exception.message
    ) : Result<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
}

/** Ejecuta [block] si el resultado es [Result.Success]. Encadenable. */
inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (this is Result.Success) block(data)
    return this
}

/** Ejecuta [block] si el resultado es [Result.Error]. Encadenable. */
inline fun <T> Result<T>.onError(block: (Result.Error) -> Unit): Result<T> {
    if (this is Result.Error) block(this)
    return this
}

/** Devuelve el dato si es [Result.Success], o `null` en caso contrario. */
fun <T> Result<T>.getOrNull(): T? = if (this is Result.Success) data else null

/** Devuelve el dato si es [Result.Success], o [default] en caso contrario. */
fun <T> Result<T>.getOrDefault(default: T): T = if (this is Result.Success) data else default
