package com.cielo.applibros.data.remote

import android.util.Log
import com.cielo.applibros.data.remote.api.GutendexApiService
import com.cielo.applibros.data.remote.api.OpenLibraryApiService
import com.cielo.applibros.data.remote.api.GoogleBooksApiService
import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.model.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class UnifiedBookSearchService(
    private val gutendexApi: GutendexApiService,
    private val openLibraryApi: OpenLibraryApiService,
    private val googleBooksApi: GoogleBooksApiService
) {

    suspend fun searchBooks(
        query: String,
        language: Language = Language.SPANISH
    ): List<Book> = withContext(Dispatchers.IO) {  // ✅ Cambio 1: withContext en vez de coroutineScope

        val results = mutableListOf<Book>()
        val errors = mutableListOf<Throwable>()

        // ✅ Cambio 2: Usar supervisorScope para evitar cancelación en cascada
        supervisorScope {
            val gutendexDeferred = async {
                try { searchGutendex(query) }
                catch (e: Exception) {
                    errors.add(e)
                    emptyList()
                }
            }

            val openLibraryDeferred = async {
                try { searchOpenLibrary(query) }
                catch (e: Exception) {
                    errors.add(e)
                    emptyList()
                }
            }

            val googleBooksDeferred = async {
                try { searchGoogleBooks(query) }
                catch (e: Exception) {
                    errors.add(e)
                    emptyList()
                }
            }

            // Esperar todas sin que se cancelen entre sí
            results.addAll(gutendexDeferred.await())
            results.addAll(openLibraryDeferred.await())
            results.addAll(googleBooksDeferred.await())
        }

        // ✅ Solo lanza excepción si las 3 fallaron Y no hay resultados
        if (results.isEmpty() && errors.size == 3) {
            Log.e("BookSearch", "Todas las APIs fallaron: ${errors.map { it.message }}")
            throw errors.first()
        }

        // Filtrar por idioma
        val filteredResults = results.filter { book ->
            book.languages.any { it.contains(language.code, ignoreCase = true) }
        }

        val finalResults = filteredResults.ifEmpty { results }

        // Eliminar duplicados
        finalResults.distinctBy {
            "${it.title.lowercase()}_${it.authorsString.lowercase()}"
        }
    }

    // ⬇️ Los métodos privados permanecen igual ⬇️

    private suspend fun searchGutendex(query: String): List<Book> {
        val response = gutendexApi.searchBooks(query)
        return response.results.map { dto ->
            Book(
                id = dto.id,
                title = dto.title,
                authors = dto.authors.map { it.name },
                subjects = dto.subjects,
                languages = dto.languages,
                formats = dto.formats
            )
        }
    }

    private suspend fun searchOpenLibrary(query: String): List<Book> {
        val response = openLibraryApi.searchBooks(query)
        return response.docs.map { dto ->
            Book(
                id = dto.key.hashCode() + 1_000_000,
                title = dto.title ?: "Unknown",
                authors = dto.authorName ?: listOf("Unknown"),
                subjects = dto.subject?.take(5) ?: emptyList(),
                languages = dto.language ?: emptyList(),
                formats = buildMap {
                    dto.toCoverUrl()?.let { put("image/jpeg", it) }
                }
            )
        }
    }

    private suspend fun searchGoogleBooks(query: String): List<Book> {
        val response = googleBooksApi.searchBooks(query)
        return response.items?.map { item ->
            Book(
                id = item.id.hashCode() + 2_000_000,
                title = item.volumeInfo.title ?: "Unknown",
                authors = item.volumeInfo.authors ?: listOf("Unknown"),
                subjects = item.volumeInfo.categories ?: emptyList(),
                languages = item.volumeInfo.language?.let { listOf(it) } ?: emptyList(),
                formats = buildMap {
                    item.volumeInfo.imageLinks?.thumbnail
                        ?.replace("http://", "https://")
                        ?.let { put("image/jpeg", it) }
                }
            )
        } ?: emptyList()
    }
}
