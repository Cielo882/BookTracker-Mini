package com.cielo.applibros.utils

import android.database.sqlite.SQLiteException
import com.cielo.applibros.domain.model.AppError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {

    fun mapToAppError(throwable: Throwable): AppError {
        return when (throwable) {
            // Sin internet
            is UnknownHostException -> AppError.NetworkError(
                message = "No hay conexión a internet",
                userMessage = "📡 Sin conexión a internet\nVerifica tu conexión y vuelve a intentar",
                cause = throwable
            )

            // Timeout
            is SocketTimeoutException -> AppError.TimeoutError(
                message = "Tiempo de espera agotado",
                userMessage = "⏱️ La petición tardó demasiado\nIntenta de nuevo",
                cause = throwable
            )

            // Error de red genérico
            is IOException -> AppError.NetworkError(
                message = "Error de red",
                userMessage = "🌐 Error de conexión\nVerifica tu internet e intenta de nuevo",
                cause = throwable
            )

            // Error HTTP de la API
            is HttpException -> {
                val code = throwable.code()
                when (code) {
                    404 -> AppError.ApiError(
                        code = code,
                        message = "No encontrado",
                        userMessage = "🔍 No se encontró lo que buscabas",
                        cause = throwable
                    )
                    500, 502, 503 -> AppError.ApiError(
                        code = code,
                        message = "Error del servidor",
                        userMessage = "🔧 El servidor tiene problemas\nIntenta más tarde",
                        cause = throwable
                    )
                    429 -> AppError.ApiError(
                        code = code,
                        message = "Demasiadas peticiones",
                        userMessage = "⏸️ Demasiadas búsquedas\nEspera un momento e intenta de nuevo",
                        cause = throwable
                    )
                    else -> AppError.ApiError(
                        code = code,
                        message = "Error HTTP $code",
                        userMessage = "❌ Error del servidor ($code)\nIntenta más tarde",
                        cause = throwable
                    )
                }
            }

            // Error de BD
            is SQLiteException -> AppError.DatabaseError(
                message = "Error de base de datos",
                userMessage = "💾 Error al guardar\nIntenta de nuevo o reinicia la app",
                cause = throwable
            )

            // Error desconocido
            else -> AppError.UnknownError(
                message = throwable.message ?: "Error desconocido",
                userMessage = "😕 Algo salió mal\nPor favor intenta de nuevo",
                cause = throwable
            )
        }
    }
}