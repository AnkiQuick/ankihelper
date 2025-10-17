package com.lmyby.ankiquicker.data.book

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Room entity for Book
 * Replaces Book POJO model
 *
 * Table: book
 * Primary key: id (creation epoch time in milliseconds)
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Entity(tableName = "book")
data class BookEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Long = 0,

    @ColumnInfo(name = "lastopentime")
    var lastOpenTime: Long = 0,

    @ColumnInfo(name = "bookname")
    var bookName: String = "",

    @ColumnInfo(name = "author")
    var author: String = "",

    @ColumnInfo(name = "bookpath")
    var bookPath: String = "",

    @ColumnInfo(name = "readposition")
    var readPosition: String = "" // stored in JSON format
) {
    // Constructor for creating from existing data
    @Ignore
    constructor(
        id: Long,
        lastOpenTime: Long,
        bookName: String,
        author: String,
        bookPath: String,
        readPosition: String,
        dummy: Boolean
    ) : this(id, lastOpenTime, bookName, author, bookPath, readPosition)
}
