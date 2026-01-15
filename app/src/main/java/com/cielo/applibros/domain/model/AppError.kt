package com.cielo.applibros.domain.model

sealed class AppError(
    open val message: String,
    open val userMessage: String,
    open val cause: Throwable? = null
) {

    // Error de red
    data class NetworkError(
        override val message: String = "Error de conexión",
        override val userMessage: String = "No se pudo conectar a internet. Verifica tu conexión.",
        override val cause: Throwable? = null
    ) : AppError(message, userMessage, cause)

    // Error de timeout
    data class TimeoutError(
        override val message: String = "Timeout",
        override val userMessage: String = "La petición tardó demasiado. Intenta de nuevo.",
        override val cause: Throwable? = null
    ) : AppError(message, userMessage, cause)

    // Error de API
    data class ApiError(
        val code: Int = 0,
        override val message: String = "Error del servidor",
        override val userMessage: String = "El servidor no está disponible. Intenta más tarde.",
        override val cause: Throwable? = null
    ) : AppError(message, userMessage, cause)

    // Error de base de datos
    data class DatabaseError(
        override val message: String = "Error de base de datos",
        override val userMessage: String = "Hubo un problema al guardar. Intenta de nuevo.",
        override val cause: Throwable? = null
    ) : AppError(message, userMessage, cause)

    // Error desconocido
    data class UnknownError(
        override val message: String = "Error desconocido",
        override val userMessage: String = "Algo salió mal. Por favor intenta de nuevo.",
        override val cause: Throwable? = null
    ) : AppError(message, userMessage, cause)

    // Sin resultados (no es error técnico, pero es un estado)
    data class NoResultsError(
        override val message: String = "Sin resultados",
        override val userMessage: String = "No se encontraron resultados para tu búsqueda.",
        override val cause: Throwable? = null
    ) : AppError(message, userMessage, cause)
}
