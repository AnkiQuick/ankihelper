package com.mmjang.ankihelper.data.dict;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mmjang.ankihelper.data.dict.Form; // Import the Form class

@Database(entities = {MaldpeEntry.class, Form.class}, version = 1)
public abstract class MaldpeDatabase extends RoomDatabase {

    public abstract MaldpeDao maldpeDao();

    private static MaldpeDatabase instance;

    public static synchronized MaldpeDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MaldpeDatabase.class, "maldpe.db")
                    .allowMainThreadQueries() // For demonstration, don't use this in production
                    .build();
        }
        return instance;
    }
}