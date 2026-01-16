package com.cielo.applibros.data.remote

import com.cielo.applibros.data.remote.api.GutendexApiService
import com.cielo.applibros.data.remote.api.OpenLibraryApiService
import com.cielo.applibros.data.remote.api.GoogleBooksApiService
import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.model.Language
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
class UnifiedBookSearchService(
    private val gutendexApi: GutendexApiService,
    private val openLibraryApi: OpenLibraryApiService,
    private val googleBooksApi: GoogleBooksApiService
) {

    suspend fun searchBooks(
        query: String,
        language: Language = Language.SPANISH
    ): List<Book> = coroutineScope {

        val gutendexDeferred = async { searchGutendex(query) }
        val openLibraryDeferred = async { searchOpenLibrary(query) }
        val googleBooksDeferred = async { searchGoogleBooks(query) }

        val results = mutableListOf<Book>()
        val errors = mutableListOf<Throwable>()

        listOf(
            gutendexDeferred,
            openLibraryDeferred,
            googleBooksDeferred
        ).forEach { deferred ->
            try {
                results.addAll(deferred.await())
            } catch (t: Throwable) {
                errors.add(t)
            }
        }

        // ❌ TODAS las APIs fallaron → error real
        if (results.isEmpty() && errors.isNotEmpty()) {
            throw errors.first()
        }

        //  Filtrar por idioma
        val filteredResults = results.filter { book ->
            book.languages.any { it.contains(language.code, ignoreCase = true) }
        }

        val finalResults = filteredResults.ifEmpty { results }

        finalResults.distinctBy {
            "${it.title.lowercase()}_${it.authorsString.lowercase()}"
        }
    }

    // ⬇️ SIN try/catch ⬇️

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
