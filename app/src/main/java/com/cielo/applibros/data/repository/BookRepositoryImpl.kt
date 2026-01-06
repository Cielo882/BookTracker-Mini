// data/repository/BookRepositoryImpl.kt
package com.cielo.applibros.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.cielo.applibros.data.local.dao.BookDao
import com.cielo.applibros.data.local.entities.BookEntity
import com.cielo.applibros.data.local.preferences.SettingsPreferences
import com.cielo.applibros.data.remote.UnifiedBookSearchService
import com.cielo.applibros.domain.model.Book
import com.cielo.applibros.domain.model.ReadingStatus
import com.cielo.applibros.domain.repository.BookRepository

class BookRepositoryImpl(
    private val unifiedSearchService: UnifiedBookSearchService, // CAMBIO
    private val bookDao: BookDao,
    private val settingsPreferences: SettingsPreferences // Add this line
) : BookRepository {

    override suspend fun searchBooks(query: String): List<Book> {
        // OBTENER idioma desde SharedPreferences
        val language = settingsPreferences.getSettings().language

        return unifiedSearchService.searchBooks(query, language)
    }

    // ... resto de métodos igual ...
    override suspend fun addToReadList(book: Book) {
        bookDao.insertBook(book.toEntity())
    }

    override suspend fun removeFromReadList(book: Book) {
        bookDao.getBookById(book.id)?.let { bookEntity ->
            bookDao.deleteBook(bookEntity)
        }
    }

    override suspend fun getReadBooks(): List<Book> {
        return bookDao.getFinishedBooks().value?.map { it.toDomainModel() } ?: emptyList()
    }

    override fun getAllBooks(): LiveData<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getBooksByStatus(status: ReadingStatus): LiveData<List<Book>> {
        return bookDao.getBooksByStatus(status).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getBooksToRead(): LiveData<List<Book>> {
        return bookDao.getBooksToRead().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCurrentlyReadingLive(): LiveData<List<Book>> {
        return bookDao.getCurrentlyReading().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFinishedBooks(): LiveData<List<Book>> {
        return bookDao.getFinishedBooks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateReadingStatus(bookId: Int, status: ReadingStatus) {
        bookDao.updateReadingStatus(bookId, status)
    }

    override suspend fun updateRating(bookId: Int, rating: Int?) {
        bookDao.updateRating(bookId, rating)
    }

    override suspend fun updateReview(bookId: Int, review: String?) {
        bookDao.updateReview(bookId, review)
    }

    override suspend fun updateFavorite(bookId: Int, isFavorite: Boolean) {
        bookDao.updateFavorite(bookId, isFavorite)
    }

    override suspend fun updateStartDate(bookId: Int, startDate: Long?) {
        bookDao.updateStartDate(bookId, startDate)
    }

    override suspend fun updateFinishDate(bookId: Int, finishDate: Long?) {
        bookDao.updateFinishDate(bookId, finishDate)
    }

    override suspend fun getTotalBooksRead(): Int {
        return bookDao.getTotalBooksRead()
    }

    override suspend fun getTotalBooksToRead(): Int {
        return bookDao.getTotalBooksToRead()
    }

    override suspend fun getAverageRating(): Double? {
        return bookDao.getAverageRating()
    }

    override suspend fun getCurrentlyReading(): List<Book> {
        return bookDao.getCurrentlyReadingList().map { it.toDomainModel() }
    }

    override suspend fun getFavoriteBooks(): List<Book> {
        return bookDao.getFavoriteBooksList().map { it.toDomainModel() }

    }

    override suspend fun getBooksToReadList(): List<Book> {
        return bookDao.getBooksToReadList().map { it.toDomainModel() }
    }

    override suspend fun getCurrentlyReadingList(): List<Book> {
        return bookDao.getCurrentlyReadingList().map { it.toDomainModel() }
    }

    override suspend fun clearAllBooks() {
        bookDao.deleteAllBooks()
    }

    // ✅ NUEVO: Método suspendido
    override suspend fun getFinishedBooksList(): List<Book> {
        return bookDao.getFinishedBooksList().map { it.toDomainModel() }
    }
}

// Extension functions
private fun BookEntity.toDomainModel(): Book {
    return Book(
        id = this.id,
        title = this.title,
        authors = this.authors,
        subjects = this.subjects,
        languages = this.languages,
        formats = this.formats,
        readingStatus = this.readingStatus,
        rating = this.rating,
        startDate = this.startDate,
        finishDate = this.finishDate,
        review = this.review,
        isFavorite = this.isFavorite,
        dateAdded = this.dateAdded
    )
}

private fun Book.toEntity(): BookEntity {
    return BookEntity(
        id = this.id,
        title = this.title,
        authors = this.authors,
        subjects = this.subjects,
        languages = this.languages,
        formats = this.formats,
        readingStatus = this.readingStatus,
        rating = this.rating,
        startDate = this.startDate,
        finishDate = this.finishDate,
        review = this.review,
        isFavorite = this.isFavorite,
        dateAdded = this.dateAdded
    )
}