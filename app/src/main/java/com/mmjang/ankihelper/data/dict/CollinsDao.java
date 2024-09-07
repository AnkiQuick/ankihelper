package com.mmjang.ankihelper.data.dict;

import androidx.room.Dao;
import androidx.room.Query;
import android.database.Cursor;

@Dao
public interface CollinsDao {

    @Query("SELECT hwd, display_hwd, phrase, phonetics, sense, ext, def_en, def_cn FROM dict WHERE hwd = :query COLLATE NOCASE")
    Cursor queryDefinition(String query);

    @Query("SELECT bases FROM forms WHERE hwd = :query")
    Cursor getForms(String query);

    @Query("SELECT rowid _id, hwd FROM hwds WHERE hwd LIKE :query")
    Cursor getFilterCursor(String query);

}