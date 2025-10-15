package com.mmjang.ankihelper.data.book

import androidx.room.*

/**
 * DAO for BookEntity
 * Provides all CRUD operations for books using suspend functions
 */
@Dao
interface BookDao {

    /**
     * Get all books sorted by last open time descending (most recent first)
     * @return List of all books
     */
    @Query("SELECT * FROM book ORDER BY lastopentime DESC")
    suspend fun getAllBooks(): List<BookEntity>

    /**
     * Get a book by ID
     * @param bookId The ID of the book
     * @return The book or null if not found
     */
    @Query("SELECT * FROM book WHERE id = :bookId")
    suspend fun getBookById(bookId: Long): BookEntity?

    /**
     * Get books by name (partial match)
     * @param bookName The name to search for
     * @return List of books matching the name
     */
    @Query("SELECT * FROM book WHERE bookname LIKE '%' || :bookName || '%' ORDER BY lastopentime DESC")
    suspend fun getBooksByName(bookName: String): List<BookEntity>

    /**
     * Get books by author (partial match)
     * @param author The author to search for
     * @return List of books by the author
     */
    @Query("SELECT * FROM book WHERE author LIKE '%' || :author || '%' ORDER BY lastopentime DESC")
    suspend fun getBooksByAuthor(author: String): List<BookEntity>

    /**
     * Get books by path
     * @param bookPath The path to search for
     * @return The book at the path or null if not found
     */
    @Query("SELECT * FROM book WHERE bookpath = :bookPath")
    suspend fun getBookByPath(bookPath: String): BookEntity?

    /**
     * Get recently opened books
     * @param limit Maximum number of books to return
     * @return List of recently opened books
     */
    @Query("SELECT * FROM book ORDER BY lastopentime DESC LIMIT :limit")
    suspend fun getRecentBooks(limit: Int): List<BookEntity>

    /**
     * Insert a new book
     * @param book The book to insert
     * @return The row ID of the inserted book
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    /**
     * Insert multiple books
     * @param books The books to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    /**
     * Update an existing book
     * @param book The book to update
     * @return Number of rows updated
     */
    @Update
    suspend fun updateBook(book: BookEntity): Int

    /**
     * Update book's last open time
     * @param bookId The ID of the book
     * @param lastOpenTime The new last open time
     * @return Number of rows updated
     */
    @Query("UPDATE book SET lastopentime = :lastOpenTime WHERE id = :bookId")
    suspend fun updateLastOpenTime(bookId: Long, lastOpenTime: Long): Int

    /**
     * Update book's read position
     * @param bookId The ID of the book
     * @param readPosition The new read position (JSON)
     * @return Number of rows updated
     */
    @Query("UPDATE book SET readposition = :readPosition WHERE id = :bookId")
    suspend fun updateReadPosition(bookId: Long, readPosition: String): Int

    /**
     * Delete a book
     * @param book The book to delete
     * @return Number of rows deleted
     */
    @Delete
    suspend fun deleteBook(book: BookEntity): Int

    /**
     * Delete a book by ID
     * @param bookId The ID of the book to delete
     * @return Number of rows deleted
     */
    @Query("DELETE FROM book WHERE id = :bookId")
    suspend fun deleteBookById(bookId: Long): Int

    /**
     * Delete all books
     * @return Number of rows deleted
     */
    @Query("DELETE FROM book")
    suspend fun deleteAllBooks(): Int

    /**
     * Check if a book exists by ID
     * @param bookId The ID of the book
     * @return True if book exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM book WHERE id = :bookId)")
    suspend fun bookExists(bookId: Long): Boolean

    /**
     * Check if a book exists by path
     * @param bookPath The path of the book
     * @return True if book exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM book WHERE bookpath = :bookPath)")
    suspend fun bookExistsByPath(bookPath: String): Boolean

    /**
     * Get count of all books
     * @return Number of books
     */
    @Query("SELECT COUNT(*) FROM book")
    suspend fun getBookCount(): Int
}
