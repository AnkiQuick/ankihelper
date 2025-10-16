package com.lmyby.ankihelper.data.book

import android.content.ContentValues
import com.lmyby.ankihelper.data.database.DBContract

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
data class Book(
    var id: Long = 0,
    var lastOpenTime: Long = 0,
    var bookName: String = "",
    var author: String = "",
    var bookPath: String = "",
    var readPosition: String = "" // json
) {
    fun getContentValues(): ContentValues {
        return ContentValues().apply {
            put(DBContract.Book.COLUMN_ID, id)
            put(DBContract.Book.COLUMN_LAST_OPEN_TIME, lastOpenTime)
            put(DBContract.Book.COLUMN_BOOK_NAME, bookName)
            put(DBContract.Book.COLUMN_AUTHOR, author)
            put(DBContract.Book.COLUMN_BOOK_PATH, bookPath)
            put(DBContract.Book.COLUMN_READ_POSITION, readPosition)
        }
    }
}
