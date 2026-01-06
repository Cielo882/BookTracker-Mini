package com.cielo.applibros.data.local.dao


import androidx.lifecycle.LiveData
import androidx.room.*
import com.cielo.applibros.data.local.entities.BookEntity
import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.model.ReadingStatus

@Dao
interface BookDao {

    // Métodos existentes actualizados
    @Query("SELECT * FROM books")
    fun getAllBooks(): LiveData<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Int): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Update
    suspend fun updateBook(book: BookEntity)

    // Nuevos métodos por estado
    @Query("SELECT * FROM books WHERE readingStatus = :status ORDER BY dateAdded DESC")
    fun getBooksByStatus(status: ReadingStatus): LiveData<List<BookEntity>>

    @Query("SELECT * FROM books WHERE readingStatus = 'TO_READ' ORDER BY dateAdded DESC")
    fun getBooksToRead(): LiveData<List<BookEntity>>

    @Query("SELECT * FROM books WHERE readingStatus = 'READING' ORDER BY startDate DESC")
    suspend fun getCurrentlyReadingList(): List<BookEntity>

    @Query("SELECT * FROM books WHERE isFavorite = 1 AND readingStatus = 'FINISHED' LIMIT 3")
    suspend fun getFavoriteBooksList(): List<BookEntity>

    @Query("SELECT * FROM books WHERE readingStatus = 'READING' ORDER BY startDate DESC")
    fun getCurrentlyReading(): LiveData<List<BookEntity>>

    @Query("SELECT * FROM books WHERE readingStatus = 'FINISHED' ORDER BY finishDate DESC")
    fun getFinishedBooks(): LiveData<List<BookEntity>>

    // Métodos para favoritos
    @Query("SELECT * FROM books WHERE isFavorite = 1 AND readingStatus = 'FINISHED' LIMIT 3")
    fun getFavoriteBooks(): LiveData<List<BookEntity>>

    // Estadísticas
    @Query("SELECT COUNT(*) FROM books WHERE readingStatus = 'FINISHED'")
    suspend fun getTotalBooksRead(): Int

    @Query("SELECT COUNT(*) FROM books WHERE readingStatus = 'TO_READ'")
    suspend fun getTotalBooksToRead(): Int

    @Query("SELECT AVG(rating) FROM books WHERE rating IS NOT NULL AND readingStatus = 'FINISHED'")
    suspend fun getAverageRating(): Double?

    // Actualizar estado específico
    @Query("UPDATE books SET readingStatus = :status WHERE id = :bookId")
    suspend fun updateReadingStatus(bookId: Int, status: ReadingStatus)

    @Query("UPDATE books SET rating = :rating WHERE id = :bookId")
    suspend fun updateRating(bookId: Int, rating: Int?)

    @Query("UPDATE books SET review = :review WHERE id = :bookId")
    suspend fun updateReview(bookId: Int, review: String?)

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :bookId")
    suspend fun updateFavorite(bookId: Int, isFavorite: Boolean)

    @Query("UPDATE books SET startDate = :startDate WHERE id = :bookId")
    suspend fun updateStartDate(bookId: Int, startDate: Long?)

    @Query("UPDATE books SET finishDate = :finishDate WHERE id = :bookId")
    suspend fun updateFinishDate(bookId: Int, finishDate: Long?)

    // ✅ AGREGAR ESTE MÉTODO NUEVO (versión suspendida)
    @Query("SELECT * FROM books WHERE readingStatus = 'FINISHED' ORDER BY finishDate DESC")
    suspend fun getFinishedBooksList(): List<BookEntity>

    // Métodos para obtener listas directas (no LiveData)
    @Query("SELECT * FROM books WHERE readingStatus = 'TO_READ' ORDER BY dateAdded DESC")
    suspend fun getBooksToReadList(): List<BookEntity>



    // Limpiar toda la base de datos
    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()
}