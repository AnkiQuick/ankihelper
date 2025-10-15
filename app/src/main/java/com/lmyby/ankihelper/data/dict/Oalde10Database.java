package com.lmyby.ankihelper.data.dict;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DummyEntity.class}, version = 2, exportSchema = false)
public abstract class Oalde10Database extends RoomDatabase {

    public abstract Oalde10Dao oalde10Dao();

    private static Oalde10Database instance;

    public static synchronized Oalde10Database getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    Oalde10Database.class, "oaldpe10.db")
                    .createFromAsset("databases/oaldpe10.db")
                    .fallbackToDestructiveMigration()
                    // Removed .allowMainThreadQueries() - use coroutines for async operations
                    .build();
        }
        return instance;
    }
}