package com.mmjang.ankihelper.data.dict

import androidx.room.Dao
import androidx.room.Query
import android.database.Cursor

@Dao
interface Cdepe4Dao {

    @Query("SELECT hwd, phrase, sense, phonetics, def_en, def_cn FROM dict WHERE hwd = :query COLLATE NOCASE")
    suspend fun queryDefinition(query: String): Cursor

    @Query("SELECT bases FROM forms WHERE hwd = :query")
    suspend fun getForms(query: String): Cursor

    @Query("SELECT rowid _id, hwd FROM hwds WHERE hwd LIKE :query")
    suspend fun getFilterCursor(query: String): Cursor
}
