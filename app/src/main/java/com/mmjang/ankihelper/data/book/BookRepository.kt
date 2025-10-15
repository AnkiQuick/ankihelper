package com.mmjang.ankihelper.data.book

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Book operations
 *
 * Provides an abstraction layer for book-related database operations,
 * including querying, inserting, and managing EPUB book data.
 */
class BookRepository(private val bookDao: BookDao) {

    /**
     * Get all books, sorted by last open time (most recent first)
     * @return List of all BookEntity objects
     */
    suspend fun getAllBooks(): List<BookEntity> = withContext(Dispatchers.IO) {
        bookDao.getAllBooks()
    }

    /**
     * Get recently opened books with a limit
     * @param limit Maximum number of books to return
     * @return List of recently opened BookEntity objects
     */
    suspend fun getRecentBooks(limit: Int = 10): List<BookEntity> = withContext(Dispatchers.IO) {
        bookDao.getRecentBooks(limit)
    }

    /**
     * Get a specific book by ID
     * @param bookId The ID of the book
     * @return The BookEntity if found, null otherwise
     */
    suspend fun getBookById(bookId: Long): BookEntity? = withContext(Dispatchers.IO) {
        bookDao.getBookById(bookId)
    }

    /**
     * Search books by name
     * @param query The search query (partial match)
     * @return List of BookEntity objects with names containing the query
     */
    suspend fun searchByName(query: String): List<BookEntity> = withContext(Dispatchers.IO) {
        bookDao.getBooksByName(query)
    }

    /**
     * Search books by author
     * @param query The search query (partial match)
     * @return List of BookEntity objects with authors containing the query
     */
    suspend fun searchByAuthor(query: String): List<BookEntity> = withContext(Dispatchers.IO) {
        bookDao.getBooksByAuthor(query)
    }

    /**
     * Get a book by its file path
     * @param bookPath The file path of the book
     * @return The BookEntity if found, null otherwise
     */
    suspend fun getBookByPath(bookPath: String): BookEntity? = withContext(Dispatchers.IO) {
        bookDao.getBookByPath(bookPath)
    }

    /**
     * Check if a book exists with the given path
     * @param bookPath The file path to check
     * @return true if the book exists, false otherwise
     */
    suspend fun bookExistsByPath(bookPath: String): Boolean = withContext(Dispatchers.IO) {
        bookDao.bookExistsByPath(bookPath)
    }

    /**
     * Insert or update a book
     * Uses REPLACE conflict strategy
     * @param book The BookEntity to insert/update
     * @return The row ID of the inserted/updated book
     */
    suspend fun saveBook(book: BookEntity): Long = withContext(Dispatchers.IO) {
        bookDao.insertBook(book)
    }

    /**
     * Insert multiple books
     * @param books List of BookEntity objects to insert
     */
    suspend fun insertBooks(books: List<BookEntity>) = withContext(Dispatchers.IO) {
        bookDao.insertBooks(books)
    }

    /**
     * Update an existing book
     * @param book The BookEntity with updated data
     * @return The number of rows updated
     */
    suspend fun updateBook(book: BookEntity): Int = withContext(Dispatchers.IO) {
        bookDao.updateBook(book)
    }

    /**
     * Update the last open time for a book
     * @param bookId The ID of the book
     * @param lastOpenTime The new last open time
     * @return The number of rows updated
     */
    suspend fun updateLastOpenTime(bookId: Long, lastOpenTime: Long): Int = withContext(Dispatchers.IO) {
        bookDao.updateLastOpenTime(bookId, lastOpenTime)
    }

    /**
     * Update the read position for a book
     * @param bookId The ID of the book
     * @param readPosition The new read position (JSON format)
     * @return The number of rows updated
     */
    suspend fun updateReadPosition(bookId: Long, readPosition: String): Int = withContext(Dispatchers.IO) {
        bookDao.updateReadPosition(bookId, readPosition)
    }

    /**
     * Delete a book
     * @param book The BookEntity to delete
     * @return The number of rows deleted
     */
    suspend fun deleteBook(book: BookEntity): Int = withContext(Dispatchers.IO) {
        bookDao.deleteBook(book)
    }

    /**
     * Delete a book by ID
     * @param bookId The ID of the book to delete
     * @return The number of rows deleted
     */
    suspend fun deleteBookById(bookId: Long): Int = withContext(Dispatchers.IO) {
        bookDao.deleteBookById(bookId)
    }

    /**
     * Delete all books
     * @return The number of rows deleted
     */
    suspend fun deleteAllBooks(): Int = withContext(Dispatchers.IO) {
        bookDao.deleteAllBooks()
    }

    /**
     * Get the count of all books
     * @return The number of books in the database
     */
    suspend fun getBookCount(): Int = withContext(Dispatchers.IO) {
        bookDao.getBookCount()
    }
}
