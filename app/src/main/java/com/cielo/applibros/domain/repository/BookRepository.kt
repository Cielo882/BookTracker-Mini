package com.cielo.applibros.domain.repository

import androidx.lifecycle.LiveData
import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.model.ReadingStatus
import com.cielo.applibros.domain.model.Result

interface BookRepository {
    // Métodos existentes
    suspend fun searchBooks(query: String): Result<List<Book>>
    suspend fun getReadBooks(): List<Book>
    suspend fun addToReadList(book: Book)
    suspend fun removeFromReadList(book: Book)

    // ELIMINAR ESTE (no se usa más):
    // fun updateBookRating(bookId: Int, rating: Float)

    // Nuevos métodos
    fun getAllBooks(): LiveData<List<Book>>
    fun getBooksByStatus(status: ReadingStatus): LiveData<List<Book>>
    fun getBooksToRead(): LiveData<List<Book>>
    fun getCurrentlyReadingLive(): LiveData<List<Book>>
    fun getFinishedBooks(): LiveData<List<Book>>


    //  NUEVO: Método suspendido para obtener lista directa
    suspend fun getFinishedBooksList(): List<Book>

    suspend fun updateReadingStatus(bookId: Int, status: ReadingStatus)
    suspend fun updateRating(bookId: Int, rating: Int?)
    suspend fun updateReview(bookId: Int, review: String?)
    suspend fun updateFavorite(bookId: Int, isFavorite: Boolean)
    suspend fun updateStartDate(bookId: Int, startDate: Long?)
    suspend fun updateFinishDate(bookId: Int, finishDate: Long?)

    suspend fun getTotalBooksRead(): Int
    suspend fun getTotalBooksToRead(): Int
    suspend fun getAverageRating(): Double?
    suspend fun getCurrentlyReading(): List<Book>
    suspend fun getFavoriteBooks(): List<Book>

    suspend fun getBooksToReadList(): List<Book>
    suspend fun getCurrentlyReadingList(): List<Book>
    suspend fun clearAllBooks()
}