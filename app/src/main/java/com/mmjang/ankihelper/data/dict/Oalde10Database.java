package com.mmjang.ankihelper.data.dict;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Oalde10Entry.class, Form.class}, version = 1)
public abstract class Oalde10Database extends RoomDatabase {

    public abstract Oalde10Dao oalde10Dao();

    private static Oalde10Database instance;

    public static synchronized Oalde10Database getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    Oalde10Database.class, "oaldpe10.db")
                    .allowMainThreadQueries() // For demonstration, don't use this in production
                    .build();
        }
        return instance;
    }
}