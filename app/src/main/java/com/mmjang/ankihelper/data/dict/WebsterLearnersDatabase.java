package com.mmjang.ankihelper.data.dict;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {WebsterLearnersEntry.class, Form.class}, version = 1)
public abstract class WebsterLearnersDatabase extends RoomDatabase {

    public abstract WebsterLearnersDao websterLearnersDao();

    private static WebsterLearnersDatabase instance;

    public static synchronized WebsterLearnersDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    WebsterLearnersDatabase.class, "wb_headwords.db")
                    .allowMainThreadQueries() // For demonstration, don't use this in production
                    .build();
        }
        return instance;
    }
}