package com.mmjang.ankihelper.data.dict;
import androidx.room.Dao;
import androidx.room.Query;
import android.database.Cursor;
@Dao
public interface FormsDao {
    @Query("SELECT bases FROM forms WHERE hwd = :query")
    String getForms(String query);
}