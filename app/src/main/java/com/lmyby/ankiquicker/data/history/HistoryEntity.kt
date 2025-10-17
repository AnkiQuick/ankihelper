package com.lmyby.ankiquicker.data.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Room entity for History
 * Replaces HistoryPOJO and History (LitePal) models
 *
 * Table: history
 * Primary key: timestamp (unique timestamp in milliseconds)
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    var timeStamp: Long = 0,

    @ColumnInfo(name = "type")
    var type: Int = 0,

    @ColumnInfo(name = "word")
    var word: String = "",

    @ColumnInfo(name = "sentence")
    var sentence: String = "",

    @ColumnInfo(name = "dictionary")
    var dictionary: String = "",

    @ColumnInfo(name = "definition")
    var definition: String = "",

    @ColumnInfo(name = "translation")
    var translation: String = "",

    @ColumnInfo(name = "note")
    var note: String = "",

    @ColumnInfo(name = "tag")
    var tag: String = ""
) {
    // Constructor for creating from existing data
    @Ignore
    constructor(
        timeStamp: Long,
        type: Int,
        word: String?,
        sentence: String?,
        dictionary: String?,
        definition: String?,
        translation: String?,
        note: String?,
        tag: String?,
        dummy: Boolean
    ) : this(
        timeStamp = timeStamp,
        type = type,
        word = word ?: "",
        sentence = sentence ?: "",
        dictionary = dictionary ?: "",
        definition = definition ?: "",
        translation = translation ?: "",
        note = note ?: "",
        tag = tag ?: ""
    )
}
