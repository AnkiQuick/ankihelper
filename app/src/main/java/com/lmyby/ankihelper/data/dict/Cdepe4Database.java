package com.lmyby.ankihelper.data.dict;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.lmyby.ankihelper.data.dict.Form; // Import the Form class

@Database(entities = {DummyEntity.class}, version = 2, exportSchema = false)
public abstract class Cdepe4Database extends RoomDatabase {

    public abstract Cdepe4Dao cdepe4Dao();

    private static Cdepe4Database instance;

    public static synchronized Cdepe4Database getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    Cdepe4Database.class, "cdepe4.db")
                    .createFromAsset("databases/cdepe4.db")
                    .fallbackToDestructiveMigration()
                    // Removed .allowMainThreadQueries() - use coroutines for async operations
                    .build();
        }
        return instance;
    }
}
