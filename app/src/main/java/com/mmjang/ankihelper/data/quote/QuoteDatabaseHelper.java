package com.mmjang.ankihelper.data.quote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class QuoteDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "quote.db";
    private static final int DATABASE_VERSION = 1;

    public QuoteDatabaseHelper(Context context) {
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