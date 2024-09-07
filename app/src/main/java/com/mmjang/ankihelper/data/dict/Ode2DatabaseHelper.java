package com.mmjang.ankihelper.data.dict;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Ode2DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ode2_v2.db";
    private static final int DATABASE_VERSION = 1;

    public Ode2DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // You don't need to create the table here because the database is already pre-populated.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database schema updates if needed.
    }
}