package com.mmjang.ankihelper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mmjang.ankihelper.data.book.BookDao
import com.mmjang.ankihelper.data.book.BookEntity
import com.mmjang.ankihelper.data.history.HistoryDao
import com.mmjang.ankihelper.data.history.HistoryEntity
import com.mmjang.ankihelper.data.model.UserTagDao
import com.mmjang.ankihelper.data.model.UserTagEntity
import com.mmjang.ankihelper.data.plan.OutputPlanDao
import com.mmjang.ankihelper.data.plan.OutputPlanEntity

/**
 * Main Room database for the application
 *
 * Consolidates all database operations previously split between:
 * - DatabaseManager (direct SQLite for Plan, History, Book)
 * - LitePal (for UserTag)
 *
 * Version: 4 (higher than DatabaseHelper's version 3)
 * Database name: ankihelper.db
 */
@Database(
    entities = [
        OutputPlanEntity::class,
        HistoryEntity::class,
        BookEntity::class,
        UserTagEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAO accessors
    abstract fun outputPlanDao(): OutputPlanDao
    abstract fun historyDao(): HistoryDao
    abstract fun bookDao(): BookDao
    abstract fun userTagDao(): UserTagDao

    companion object {
        private const val DATABASE_NAME = "ankihelper.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get the singleton instance of AppDatabase
         * Thread-safe with double-check locking
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_3_4)
                .build()
        }

        /**
         * Migration from version 3 to 4
         *
         * This migration handles the transition from DatabaseHelper (SQLite) and LitePal
         * to Room. The existing tables already exist in the database, so we just need
         * to ensure the schema is compatible with Room's expectations.
         *
         * Existing tables:
         * - plan (from DatabaseHelper)
         * - history (from DatabaseHelper)
         * - book (from DatabaseHelper)
         * - usertag (from LitePal, table name needs verification)
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // The tables already exist from DatabaseHelper and LitePal
                // We only need to create the usertag table if it doesn't exist
                // (LitePal may use a different table name)

                // Create usertag table if it doesn't exist
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS usertag (
                        tag TEXT NOT NULL PRIMARY KEY
                    )
                    """.trimIndent()
                )

                // Verify other tables exist with correct schema
                // If tables exist but have different schema, Room will handle validation
                // If they don't exist, create them (shouldn't happen in normal migration)

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS plan (
                        planname TEXT NOT NULL PRIMARY KEY,
                        dictionarykey TEXT,
                        outputdeckid INTEGER NOT NULL,
                        outputmodelid INTEGER NOT NULL,
                        fieldsmap TEXT
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS history (
                        timestamp INTEGER NOT NULL PRIMARY KEY,
                        type INTEGER NOT NULL,
                        word TEXT,
                        sentence TEXT,
                        dictionary TEXT,
                        definition TEXT,
                        translation TEXT,
                        note TEXT,
                        tag TEXT
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS book (
                        id INTEGER NOT NULL PRIMARY KEY,
                        lastopentime INTEGER NOT NULL,
                        bookname TEXT,
                        author TEXT,
                        bookpath TEXT,
                        readposition TEXT
                    )
                    """.trimIndent()
                )

                // Add indices for frequently queried columns
                database.execSQL("CREATE INDEX IF NOT EXISTS index_history_timestamp ON history(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_history_word ON history(word)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_book_lastopentime ON book(lastopentime)")
            }
        }

        /**
         * For testing - create an in-memory database
         */
        fun getInMemoryDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
                .allowMainThreadQueries() // Only for testing
                .build()
        }

        /**
         * Close the database and clear the instance
         * Useful for testing or when the app is being destroyed
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
