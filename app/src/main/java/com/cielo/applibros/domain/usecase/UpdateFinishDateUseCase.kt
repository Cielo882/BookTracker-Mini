package com.cielo.applibros.domain.usecase


import com.cielo.applibros.domain.repository.BookRepository
import com.cielo.applibros.utils.ErrorMapper
import com.cielo.applibros.domain.model.Result

class UpdateFinishDateUseCase(
    private val repository: BookRepository
) {
    suspend operator fun invoke(
        bookId: Int,
        finishDate: Long
    ): Result<Unit> {
        return try {
            repository.updateFinishDate(bookId, finishDate)
            Result.Success(Unit)
        } catch (t: Throwable) {
            Result.Error(ErrorMapper.mapToAppError(t))
        }
    }
}
