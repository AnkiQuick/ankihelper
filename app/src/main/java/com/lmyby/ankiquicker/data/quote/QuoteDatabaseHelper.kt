package com.lmyby.ankiquicker.data.quote

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class QuoteDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // You don't need to create the table here because the database is already pre-populated.
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database schema updates if needed.
    }

    companion object {
        private const val DATABASE_NAME = "quote.db"
        private const val DATABASE_VERSION = 1
    }
}
