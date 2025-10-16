package com.lmyby.ankihelper.data.content

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.lmyby.ankihelper.data.database.DatabaseContext
import com.lmyby.ankihelper.util.Constant
import java.io.File

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class ContentDatabaseHelper(
    context: Context,
    dbName: String
) : SQLiteOpenHelper(
    DatabaseContext(context),
    Constant.STORAGE_CONTENT_SUBDIRECTORY + File.separator + dbName,
    null,
    VERSION
) {
    private val mContext: Context = context

    override fun onCreate(db: SQLiteDatabase) {
        // Empty implementation
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Empty implementation
    }

    companion object {
        private const val VERSION = 1
    }
}
