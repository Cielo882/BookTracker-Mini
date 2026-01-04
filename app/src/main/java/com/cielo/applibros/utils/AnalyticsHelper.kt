package com.cielo.applibros.utils

import android.os.Bundle
import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.model.ReadingStatus
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

/**
 * Helper class para logging de eventos en Firebase Analytics
 */
    class AnalyticsHelper(private val analytics: FirebaseAnalytics) {

        // ========================================
        // EVENTOS DE LIBROS
        // ========================================

        fun logBookAdded(book: Book, status: ReadingStatus) {
            analytics.logEvent("book_added") {
                param("book_title", book.title)
                param("book_author", book.authorsString)
                param("status", status.name)
            }
        }

        fun logBookStatusChanged(book: Book, oldStatus: ReadingStatus, newStatus: ReadingStatus) {
            analytics.logEvent("book_status_changed") {
                param("book_title", book.title)
                param("old_status", oldStatus.name)
                param("new_status", newStatus.name)
            }
        }

        fun logBookRated(book: Book, rating: Int) {
            analytics.logEvent("book_rated") {
                param("book_title", book.title)
                param("rating", rating.toLong())
            }
        }

        fun logBookMarkedFavorite(book: Book, isFavorite: Boolean) {
            analytics.logEvent("book_favorite_toggled") {
                param("book_title", book.title)
                param("is_favorite", if (isFavorite) "true" else "false")
            }
        }

        fun logBookFinished(book: Book, daysToRead: Int?) {
            analytics.logEvent("book_finished") {
                param("book_title", book.title)
                param("book_author", book.authorsString)
                daysToRead?.let { param("days_to_read", it.toLong()) }
            }
        }

        // ========================================
        // EVENTOS DE BÚSQUEDA
        // ========================================

        fun logBookSearch(query: String, resultsCount: Int) {
            analytics.logEvent(FirebaseAnalytics.Event.SEARCH) {
                param(FirebaseAnalytics.Param.SEARCH_TERM, query)
                param("results_count", resultsCount.toLong())
            }
        }

        fun logBookSearchSource(source: String) {
            analytics.logEvent("search_source_used") {
                param("source", source) // "gutendex", "openlibrary", "googlebooks"
            }
        }

        // ========================================
        // EVENTOS DE NAVEGACIÓN
        // ========================================

        fun logScreenView(screenName: String) {
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            }
        }

        fun logFragmentOpened(fragmentName: String) {
            analytics.logEvent("fragment_opened") {
                param("fragment_name", fragmentName)
            }
        }

        // ========================================
        // EVENTOS DE CONFIGURACIÓN
        // ========================================

        fun logThemeChanged(theme: String) {
            analytics.logEvent("theme_changed") {
                param("theme_mode", theme) // "LIGHT", "DARK", "SYSTEM"
            }
        }

        fun logFeatureUsed(featureName: String) {
            analytics.logEvent("feature_used") {
                param("feature", featureName)
            }
        }

        // ========================================
        // EVENTOS DE ESTADÍSTICAS
        // ========================================

        fun logStatisticsViewed(totalBooks: Int, avgRating: Double) {
            analytics.logEvent("statistics_viewed") {
                param("total_books", totalBooks.toLong())
                param("average_rating", avgRating)
            }
        }

        fun logChartViewed(chartType: String) {
            analytics.logEvent("chart_viewed") {
                param("chart_type", chartType)
            }
        }

        // ========================================
        // PROPIEDADES DE USUARIO
        // ========================================

        fun setUserProperties(totalBooksRead: Int, favoriteGenre: String?) {
            analytics.setUserProperty("total_books_read", totalBooksRead.toString())
            favoriteGenre?.let {
                analytics.setUserProperty("favorite_genre", it)
            }
        }
    }