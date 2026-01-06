package com.cielo.applibros.domain.usecase

import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.repository.BookRepository

class GetAllBooksUseCase(
    private val repository: BookRepository
) {
    suspend fun execute(): List<Book> {
        val toRead = repository.getBooksToReadList()
        val reading = repository.getCurrentlyReadingList()
        val finished = repository.getFinishedBooksList()

        return toRead + reading + finished
    }

}