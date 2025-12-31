package com.cielo.applibros.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.lang.Exception

/**
 * Helper class para logging de errores en Firebase Crashlytics
 */
class CrashlyticsHelper(private val crashlytics: FirebaseCrashlytics) {

    // ========================================
    // LOGGING DE ERRORES
    // ========================================

    /**
     * Reporta una excepción no fatal
     */
    fun logException(exception: Exception, context: String = "") {
        crashlytics.log("Exception in: $context")
        crashlytics.recordException(exception)
    }

    /**
     * Reporta un error con mensaje personalizado
     */
    fun logError(message: String, tag: String = "AppError") {
        crashlytics.log("[$tag] $message")
    }

    /**
     * Reporta error de red
     */
    fun logNetworkError(endpoint: String, errorMessage: String) {
        crashlytics.log("Network Error - Endpoint: $endpoint")
        crashlytics.log("Error: $errorMessage")
        crashlytics.setCustomKey("last_network_error", endpoint)
    }

    /**
     * Reporta error de base de datos
     */
    fun logDatabaseError(operation: String, exception: Exception) {
        crashlytics.log("Database Error - Operation: $operation")
        crashlytics.recordException(exception)
        crashlytics.setCustomKey("last_db_operation", operation)
    }

    // ========================================
    // CUSTOM KEYS
    // ========================================

    /**
     * Agrega información del usuario actual
     */
    fun setUserInfo(userId: String? = null, totalBooks: Int = 0) {
        userId?.let { crashlytics.setUserId(it) }
        crashlytics.setCustomKey("total_books", totalBooks)
    }

    /**
     * Agrega información del libro actual
     */
    fun setCurrentBook(bookTitle: String, bookId: Int) {
        crashlytics.setCustomKey("current_book_title", bookTitle)
        crashlytics.setCustomKey("current_book_id", bookId)
    }

    /**
     * Agrega información de la última acción
     */
    fun setLastAction(action: String) {
        crashlytics.setCustomKey("last_action", action)
    }

    // ========================================
    // BREADCRUMBS (Rastro de navegación)
    // ========================================

    fun logNavigationEvent(destination: String) {
        crashlytics.log("Navigation: $destination")
    }

    fun logUserAction(action: String, details: String = "") {
        crashlytics.log("User Action: $action ${if (details.isNotEmpty()) "- $details" else ""}")
    }

    // ========================================
    // TESTING (Solo para desarrollo)
    // ========================================

    /**
     * Fuerza un crash para probar Crashlytics
     * ⚠️ SOLO USAR EN DEBUG
     */
    fun forceCrashForTesting() {
        throw RuntimeException("Test Crash from BookTracker")
    }

    /**
     * Registra un error de prueba
     */
    fun testNonFatalError() {
        try {
            throw Exception("Test non-fatal exception")
        } catch (e: Exception) {
            logException(e, "Testing Crashlytics")
        }
    }
}