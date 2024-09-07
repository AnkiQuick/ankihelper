package com.mmjang.ankihelper.data.dict;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CollinsDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "collins_v2.db";
    private static final int DATABASE_VERSION = 1;

    public CollinsDatabaseHelper(Context context) {
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