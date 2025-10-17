package com.lmyby.ankiquicker.data.dict

import androidx.room.Dao
import androidx.room.Query
import androidx.room.SkipQueryVerification
import android.database.Cursor

@Dao
interface Oalde10Dao {

    @SkipQueryVerification
    @Query("SELECT hwd, phrase, sense, phonetics, def_en, def_cn FROM dict WHERE hwd = :query COLLATE NOCASE")
    fun queryDefinition(query: String): Cursor

    @SkipQueryVerification
    @Query("SELECT bases FROM forms WHERE hwd = :query COLLATE NOCASE")
    fun getForms(query: String): Cursor

    @SkipQueryVerification
    @Query("SELECT rowid AS _id, hwd FROM hwds WHERE hwd LIKE :query")
    fun getFilterCursor(query: String): Cursor
}
