package com.lmyby.ankiquicker.data.dict

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.lmyby.ankiquicker.data.database.DatabaseContext
import com.lmyby.ankiquicker.util.StorageManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
open class BaseDatabaseHelper(
    context: Context,
    private val databaseName: String,
    version: Int
) : SQLiteOpenHelper(DatabaseContext(context), databaseName, null, version) {

    private val context: Context = context
    private val storageManager: StorageManager
    private val databaseFile: File

    init {
        val preferences = context.getSharedPreferences("ankihelper_prefs", Context.MODE_PRIVATE)
        this.storageManager = StorageManager(context, preferences)
        this.databaseFile = File(storageManager.getDatabaseDir(), databaseName)

        Log.d(TAG, "Initializing dictionary database: $databaseName")
        Log.d(TAG, "Dictionary database path: ${databaseFile.absolutePath}")

        // Force copy from assets if database doesn't exist or is empty
        copyDatabaseFromAssets(context)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Database should be copied from assets
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle upgrades if necessary
    }

    private fun copyDatabaseFromAssets(context: Context) {
        if (databaseFile.exists() && databaseFile.length() > 1000) {
            return // Database already exists and has content
        }

        if (!databaseFile.parentFile!!.exists()) {
            databaseFile.parentFile!!.mkdirs()
        }

        try {
            context.assets.open("databases/$databaseName").use { input ->
                FileOutputStream(databaseFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytes: Long = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead.toLong()
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy $databaseName from assets: ${e.message}")
            // Create minimal tables as fallback
            createFallbackDatabase()
        }
    }

    private fun createFallbackDatabase() {
        val db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS dict (hwd TEXT PRIMARY KEY, phrase TEXT, sense TEXT, phonetics TEXT, def_en TEXT, def_cn TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS hwds (rowid INTEGER PRIMARY KEY, hwd TEXT)")
        db.close()
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        if (!databaseFile.exists() || databaseFile.length() < 1000) {
            // Database missing or corrupted, attempt rebuild
            if (!databaseFile.parentFile!!.exists()) {
                databaseFile.parentFile!!.mkdirs()
            }

            copyDatabaseFromAssets(context)

            if (!databaseFile.exists() || databaseFile.length() < 1000) {
                // Still no database, create fallback
                val db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
                createFallbackDatabase()
                db.close()
            }
        }

        return super.getReadableDatabase()
    }

    companion object {
        private const val TAG = "BaseDatabaseHelper"
    }
}
