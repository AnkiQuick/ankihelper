package com.lmyby.ankihelper.data.book;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room entity for Book
 * Replaces Book POJO model
 *
 * Table: book
 * Primary key: id (creation epoch time in milliseconds)
 */
@Entity(tableName = "book")
public class BookEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "lastopentime")
    private long lastOpenTime;

    @ColumnInfo(name = "bookname")
    private String bookName;

    @ColumnInfo(name = "author")
    private String author;

    @ColumnInfo(name = "bookpath")
    private String bookPath;

    @ColumnInfo(name = "readposition")
    private String readPosition; // stored in JSON format

    // Default constructor required by Room
    public BookEntity() {
    }

    // Constructor for creating from existing data
    @Ignore
    public BookEntity(long id, long lastOpenTime, String bookName,
                     String author, String bookPath, String readPosition) {
        this.id = id;
        this.lastOpenTime = lastOpenTime;
        this.bookName = bookName;
        this.author = author;
        this.bookPath = bookPath;
        this.readPosition = readPosition;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLastOpenTime() {
        return lastOpenTime;
    }

    public void setLastOpenTime(long lastOpenTime) {
        this.lastOpenTime = lastOpenTime;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBookPath() {
        return bookPath;
    }

    public void setBookPath(String bookPath) {
        this.bookPath = bookPath;
    }

    public String getReadPosition() {
        return readPosition;
    }

    public void setReadPosition(String readPosition) {
        this.readPosition = readPosition;
    }
}
