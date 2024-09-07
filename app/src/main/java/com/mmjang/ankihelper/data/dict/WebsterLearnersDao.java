package com.mmjang.ankihelper.data.dict;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface WebsterLearnersDao {

    @Query("SELECT rowid _id, hwd FROM hwds WHERE hwd LIKE :query")
    Cursor getFilterCursor(String query);

    @Query("SELECT hwd FROM hwds WHERE hwd = :query COLLATE NOCASE")
    Cursor isWordInDict(String query);

}