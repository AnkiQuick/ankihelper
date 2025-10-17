package com.lmyby.ankiquicker.data.content

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.lmyby.ankiquicker.data.database.DatabaseContext
import com.lmyby.ankiquicker.util.StorageManager
import java.io.File

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class Content(context: Context) {
    private val dbFileList: List<File>
    private val mContext: Context
    private val storageManager: StorageManager
    private val helperList: Array<ContentDatabaseHelper?>

    init {
        mContext = context
        val preferences = context.getSharedPreferences("ankihelper_prefs", Context.MODE_PRIVATE)
        storageManager = StorageManager(context, preferences)

        val contentFolder = File(storageManager.getContentDir(), "content")
        val listOfFiles = contentFolder.listFiles()
        dbFileList = if (listOfFiles != null) {
            listOfFiles.filter { it.name.endsWith(SUFFIX) }
        } else {
            emptyList()
        }
        helperList = arrayOfNulls(dbFileList.size)
    }

    fun getContentDBList(): List<String> {
        return dbFileList.map { it.name.replace(SUFFIX, "") }
    }

    /**
     * Get content statistics for a database
     * @return List of [total count, read count] or null if database cannot be accessed
     */
    fun getCountAt(index: Int): List<Long>? {
        if (helperList[index] == null) {
            val name = dbFileList[index].name
            helperList[index] = ContentDatabaseHelper(mContext, name)
        }
        val database = try {
            helperList[index]?.writableDatabase
        } catch (e: Exception) {
            return null
        } ?: return null

        return listOf(
            DatabaseUtils.queryNumEntries(database, "content", null, null),
            DatabaseUtils.queryNumEntries(database, "content", "is_read=1", null)
        )
    }

    fun getRandomContentAt(index: Int, filterRead: Boolean): ContentEntity? {
        if (helperList[index] == null) {
            val name = dbFileList[index].name
            helperList[index] = ContentDatabaseHelper(mContext, name)
        }
        val database = try {
            helperList[index]?.writableDatabase
        } catch (e: Exception) {
            return null
        } ?: return null

        val query = if (filterRead) {
            "select id, txt, note, is_read from content " +
                    "where id in (select id from content where is_read=0 order by random() limit 1)"
        } else {
            "select id, txt, note, is_read from content where " +
                    "id in (select id from content order by random() limit 1)"
        }

        database.rawQuery(query, null).use { cursor ->
            if (cursor.count == 0) {
                return null
            }
            cursor.moveToNext()
            val id = cursor.getInt(0)
            val txt = cursor.getString(1)
            val note = cursor.getString(2)
            val isRead = cursor.getInt(3) == 1

            // Mark as read if not already read
            if (!isRead) {
                val contentValues = ContentValues().apply {
                    put("id", id)
                    put("txt", txt)
                    put("note", note)
                    put("is_read", 1)
                }
                database.update("content", contentValues, "id=$id", null)
            }

            return ContentEntity(txt, note)
        }
    }

    companion object {
        private const val SUFFIX = ".db"
    }

    /**
     * Helper class for content databases
     */
    private class ContentDatabaseHelper(
        context: Context,
        databaseName: String
    ) : SQLiteOpenHelper(DatabaseContext(context), databaseName, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            // Create content table if it doesn't exist
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS content (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    txt TEXT,
                    note TEXT,
                    is_read INTEGER DEFAULT 0)"""
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // Handle database upgrades if needed
        }

        companion object {
            private const val DATABASE_VERSION = 1
        }
    }
}
