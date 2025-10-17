package com.lmyby.ankiquicker.data.database

import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.lmyby.ankiquicker.util.StorageManager
import java.io.File

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class DatabaseContext(base: Context) : ContextWrapper(base) {

    private val storageManager: StorageManager

    init {
        val preferences = base.getSharedPreferences("ankihelper_prefs", Context.MODE_PRIVATE)
        this.storageManager = StorageManager(base, preferences)
    }

    override fun getDatabasePath(name: String): File? {
        // Always use external storage - no fallback to internal storage
        val databaseDir = storageManager.getDatabaseDir() ?: run {
            Log.e(DEBUG_CONTEXT, "Database directory is null!")
            return null
        }

        // Don't modify the database name, just use it as-is
        val result = File(databaseDir, name)

        if (!result.parentFile!!.exists()) {
            result.parentFile!!.mkdirs()
        }

        return result
    }

    /* this version is called for android devices >= api-11. thank to @damccull for fixing this. */
    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?,
        errorHandler: DatabaseErrorHandler?
    ): SQLiteDatabase {
        return openOrCreateDatabase(name, mode, factory)
    }

    /* this version is called for android devices < api-11 */
    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?
    ): SQLiteDatabase {
        val dbPath = getDatabasePath(name)
        val result = SQLiteDatabase.openOrCreateDatabase(dbPath!!.absolutePath, null)

        Log.w(DEBUG_CONTEXT, "openOrCreateDatabase($name,,) = ${result.path}")
        return result
    }

    companion object {
        private const val DEBUG_CONTEXT = "DatabaseContext"
    }
}
