package com.cielo.applibros.domain.usecase

import com.cielo.applibros.domain.repository.BookRepository

class ClearAllBooksUseCase(
    private val repository: BookRepository
) {
    suspend fun execute() {
        repository.clearAllBooks()
    }
}