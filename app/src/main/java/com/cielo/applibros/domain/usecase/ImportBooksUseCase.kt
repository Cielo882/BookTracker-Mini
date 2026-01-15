package com.cielo.applibros.domain.usecase

import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.repository.BookRepository
import com.cielo.applibros.utils.ErrorMapper
import com.cielo.applibros.domain.model.Result


class ImportBooksUseCase(
    private val repository: BookRepository
) {
    suspend fun execute(
        books: List<Book>,
        clearExisting: Boolean = true
    ): Result<Unit> {
        return try {
            if (clearExisting) repository.clearAllBooks()
            books.forEach { repository.addToReadList(it) }
            Result.Success(Unit)
        } catch (t: Throwable) {
            Result.Error(ErrorMapper.mapToAppError(t))
        }
    }
}
