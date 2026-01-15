package com.cielo.applibros.domain.usecase

import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.repository.BookRepository
import com.cielo.applibros.domain.model.Result


class GetBooksUseCase(
    private val repository: BookRepository
) {
    suspend operator fun invoke(query: String): Result<List<Book>> {
        return repository.searchBooks(query)
    }
}
