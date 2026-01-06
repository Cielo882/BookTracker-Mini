package com.cielo.applibros.domain.usecase

import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.repository.BookRepository

class ImportBooksUseCase(
    private val repository: BookRepository
) {
    suspend fun execute(books: List<Book>, clearExisting: Boolean = true) {
        if (clearExisting) {
            repository.clearAllBooks()
        }

        books.forEach { book ->
            repository.addToReadList( book)
        }
    }
}