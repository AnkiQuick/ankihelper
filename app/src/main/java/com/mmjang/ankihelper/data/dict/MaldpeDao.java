package com.mmjang.ankihelper.data.dict;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface MaldpeDao {

    @Query("SELECT hwd, phrase, sense, phonetics, def_en, def_cn FROM dict WHERE hwd = :query COLLATE NOCASE")
    Cursor queryDefinition(String query);

    @Query("SELECT bases FROM forms WHERE hwd = :query")
    Cursor getForms(String query);

    @Query("SELECT rowid _id, hwd FROM hwds WHERE hwd LIKE :query")
    Cursor getFilterCursor(String query);

}