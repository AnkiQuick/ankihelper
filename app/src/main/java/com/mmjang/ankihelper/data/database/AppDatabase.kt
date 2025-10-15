package com.mmjang.ankihelper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mmjang.ankihelper.data.ai.AIDictionaryConfig
import com.mmjang.ankihelper.data.ai.AIDictionaryConfigDao
import com.mmjang.ankihelper.data.ai.AITranslatorConfig
import com.mmjang.ankihelper.data.ai.AITranslatorConfigDao
import com.mmjang.ankihelper.data.ai.LLMConfig
import com.mmjang.ankihelper.data.ai.LLMConfigDao
import com.mmjang.ankihelper.data.ai.TTSConfig
import com.mmjang.ankihelper.data.ai.TTSConfigDao
import com.mmjang.ankihelper.data.ai.cache.AIDictionaryCache
import com.mmjang.ankihelper.data.ai.cache.AIDictionaryCacheDao
import com.mmjang.ankihelper.data.ai.cache.AITranslatorCache
import com.mmjang.ankihelper.data.ai.cache.AITranslatorCacheDao
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
 * Manages the ankihelper.db database:
 * - All entities now managed by Room (LitePal has been fully removed)
 * - Core entities: Plan, History, Book, UserTag
 * - AI entities: LLMConfig, TTSConfig, AIDictionaryConfig, AITranslatorConfig,
 *   AIDictionaryCache, AITranslatorCache
 *
 * Version: 7 (migrated all AI entities from LitePal to Room)
 * Database name: ankihelper.db
 */
@Database(
    entities = [
        OutputPlanEntity::class,
        HistoryEntity::class,
        BookEntity::class,
        UserTagEntity::class,
        LLMConfig::class,
        TTSConfig::class,
        AIDictionaryConfig::class,
        AITranslatorConfig::class,
        AIDictionaryCache::class,
        AITranslatorCache::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAO accessors
    abstract fun outputPlanDao(): OutputPlanDao
    abstract fun historyDao(): HistoryDao
    abstract fun bookDao(): BookDao
    abstract fun userTagDao(): UserTagDao
    abstract fun llmConfigDao(): LLMConfigDao
    abstract fun ttsConfigDao(): TTSConfigDao
    abstract fun aiDictionaryConfigDao(): AIDictionaryConfigDao
    abstract fun aiTranslatorConfigDao(): AITranslatorConfigDao
    abstract fun aiDictionaryCacheDao(): AIDictionaryCacheDao
    abstract fun aiTranslatorCacheDao(): AITranslatorCacheDao

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
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE) // Avoid WAL mode issues
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        android.util.Log.d("AppDatabase", "Database created")
                    }
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        android.util.Log.d("AppDatabase", "Database opened, version: ${db.version}")
                    }
                })
                .build()
        }

        /**
         * Migration from version 3 to 4
         *
         * This migration handles the transition from DatabaseHelper (SQLite) and LitePal
         * to Room. The existing tables were created without PRIMARY KEY constraints,
         * so we need to recreate them with proper schema.
         *
         * Strategy: Create new tables, copy data, drop old, rename new
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                android.util.Log.d("AppDatabase", "Starting migration 3→4")

                try {
                    // 1. Migrate plan table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS plan_new (
                            planname TEXT NOT NULL PRIMARY KEY,
                            dictionarykey TEXT,
                            outputdeckid INTEGER NOT NULL,
                            outputmodelid INTEGER NOT NULL,
                            fieldsmap TEXT
                        )
                        """.trimIndent()
                    )
                    // Try to copy data if old table exists
                    try {
                        database.execSQL(
                            """
                            INSERT OR IGNORE INTO plan_new (planname, dictionarykey, outputdeckid, outputmodelid, fieldsmap)
                            SELECT planname, dictionarykey, outputdeckid, outputmodelid, fieldsmap FROM plan
                            """.trimIndent()
                        )
                        database.execSQL("DROP TABLE plan")
                    } catch (e: Exception) {
                        android.util.Log.d("AppDatabase", "Plan table doesn't exist or already migrated")
                    }
                    database.execSQL("ALTER TABLE plan_new RENAME TO plan")

                    // 2. Migrate history table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS history_new (
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
                    try {
                        database.execSQL(
                            """
                            INSERT OR IGNORE INTO history_new (timestamp, type, word, sentence, dictionary, definition, translation, note, tag)
                            SELECT timestamp, type, word, sentence, dictionary, definition, translation, note, tag FROM history
                            """.trimIndent()
                        )
                        database.execSQL("DROP TABLE history")
                    } catch (e: Exception) {
                        android.util.Log.d("AppDatabase", "History table doesn't exist or already migrated")
                    }
                    database.execSQL("ALTER TABLE history_new RENAME TO history")

                    // 3. Migrate book table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS book_new (
                            id INTEGER NOT NULL PRIMARY KEY,
                            lastopentime INTEGER NOT NULL,
                            bookname TEXT,
                            author TEXT,
                            bookpath TEXT,
                            readposition TEXT
                        )
                        """.trimIndent()
                    )
                    try {
                        database.execSQL(
                            """
                            INSERT OR IGNORE INTO book_new (id, lastopentime, bookname, author, bookpath, readposition)
                            SELECT id, lastopentime, bookname, author, bookpath, readposition FROM book
                            """.trimIndent()
                        )
                        database.execSQL("DROP TABLE book")
                    } catch (e: Exception) {
                        android.util.Log.d("AppDatabase", "Book table doesn't exist or already migrated")
                    }
                    database.execSQL("ALTER TABLE book_new RENAME TO book")

                    // 4. Create usertag table (from LitePal)
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS usertag (
                            tag TEXT NOT NULL PRIMARY KEY
                        )
                        """.trimIndent()
                    )

                    // 5. Create LitePal AI configuration tables (LitePal managed)
                    // LLMConfig table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS llmconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT,
                            baseUrl TEXT,
                            apiToken TEXT,
                            modelName TEXT,
                            endpointPath TEXT
                        )
                        """.trimIndent()
                    )

                    // TTSConfig table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS ttsconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT,
                            baseUrl TEXT,
                            apiToken TEXT,
                            modelName TEXT
                        )
                        """.trimIndent()
                    )

                    // AIDictionaryConfig table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS aidictionaryconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            dictionaryName TEXT,
                            llmId INTEGER,
                            prompt TEXT,
                            sourceLanguage TEXT,
                            targetLanguage TEXT
                        )
                        """.trimIndent()
                    )

                    // AITranslatorConfig table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS aitranslatorconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            translatorName TEXT,
                            llmId INTEGER,
                            prompt TEXT,
                            isDefault INTEGER,
                            sourceLanguage TEXT,
                            targetLanguage TEXT
                        )
                        """.trimIndent()
                    )

                    // AIDictionaryCache table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS aidictionarycache (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            hwd TEXT,
                            phrase TEXT,
                            sense TEXT,
                            phonetics TEXT,
                            defEn TEXT,
                            defCn TEXT,
                            example TEXT,
                            llmConfigId INTEGER,
                            timestamp INTEGER
                        )
                        """.trimIndent()
                    )

                    // AITranslatorCache table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS aitranslatorcache (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            sourceText TEXT,
                            sourceLanguage TEXT,
                            targetLanguage TEXT,
                            translatedText TEXT,
                            llmConfigId INTEGER,
                            timestamp INTEGER
                        )
                        """.trimIndent()
                    )

                    // 6. Add indices for performance
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_history_timestamp ON history(timestamp)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_history_word ON history(word)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_book_lastopentime ON book(lastopentime)")

                    android.util.Log.d("AppDatabase", "Migration 3→4 completed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Migration 3→4 failed", e)
                    throw e
                }
            }
        }

        /**
         * Migration from version 4 to 5
         *
         * This is a no-op migration to handle databases that were already upgraded to version 5
         * by a previous version of the app. The schema remains the same.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema changes needed - this migration exists only to handle
                // databases that were already at version 5
                android.util.Log.d("AppDatabase", "Migration 4→5: No changes required")
            }
        }

        /**
         * Migration from version 5 to 6
         *
         * Creates LitePal AI configuration tables. These tables were missing from earlier migrations
         * and are needed for LitePal to manage AI dictionary/translator configurations and caches.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                android.util.Log.d("AppDatabase", "Starting migration 5→6")

                try {
                    // LLMConfig table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS llmconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT,
                            baseUrl TEXT,
                            apiToken TEXT,
                            modelName TEXT,
                            endpointPath TEXT
                        )
                        """.trimIndent()
                    )

                    // TTSConfig table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS ttsconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT,
                            baseUrl TEXT,
                            apiToken TEXT,
                            modelName TEXT
                        )
                        """.trimIndent()
                    )

                    // AIDictionaryConfig table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS aidictionaryconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            dictionaryName TEXT,
                            llmId INTEGER,
                            prompt TEXT,
                            sourceLanguage TEXT,
                            targetLanguage TEXT
                        )
                        """.trimIndent()
                    )

                    // AITranslatorConfig table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS aitranslatorconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            translatorName TEXT,
                            llmId INTEGER,
                            prompt TEXT,
                            isDefault INTEGER,
                            sourceLanguage TEXT,
                            targetLanguage TEXT
                        )
                        """.trimIndent()
                    )

                    // AIDictionaryCache table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS aidictionarycache (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            hwd TEXT,
                            phrase TEXT,
                            sense TEXT,
                            phonetics TEXT,
                            defEn TEXT,
                            defCn TEXT,
                            example TEXT,
                            llmConfigId INTEGER,
                            timestamp INTEGER
                        )
                        """.trimIndent()
                    )

                    // AITranslatorCache table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS aitranslatorcache (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            sourceText TEXT,
                            sourceLanguage TEXT,
                            targetLanguage TEXT,
                            translatedText TEXT,
                            llmConfigId INTEGER,
                            timestamp INTEGER
                        )
                        """.trimIndent()
                    )

                    android.util.Log.d("AppDatabase", "Migration 5→6 completed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Migration 5→6 failed", e)
                    throw e
                }
            }
        }

        /**
         * Migration from version 6 to 7
         *
         * Migrates AI entities from LitePal management to Room management.
         * Drops and recreates AI tables to ensure they match Room's expected schema exactly.
         * This is safe because AI config data is user-entered and can be re-created if needed.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                android.util.Log.d("AppDatabase", "Starting migration 6→7")

                try {
                    // Temporarily disable foreign keys to allow dropping tables
                    database.execSQL("PRAGMA foreign_keys=OFF")

                    // Drop existing AI tables to ensure clean migration from LitePal to Room
                    // This ensures the schema matches Room's expectations exactly
                    database.execSQL("DROP TABLE IF EXISTS llmconfig")
                    database.execSQL("DROP TABLE IF EXISTS ttsconfig")
                    database.execSQL("DROP TABLE IF EXISTS aidictionaryconfig")
                    database.execSQL("DROP TABLE IF EXISTS aitranslatorconfig")
                    database.execSQL("DROP TABLE IF EXISTS aidictionarycache")
                    database.execSQL("DROP TABLE IF EXISTS aitranslatorcache")

                    // Recreate all AI tables with exact Room schema

                    // LLMConfig table
                    database.execSQL(
                        """
                        CREATE TABLE llmconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT,
                            baseUrl TEXT,
                            apiToken TEXT,
                            modelName TEXT,
                            endpointPath TEXT
                        )
                        """.trimIndent()
                    )

                    // TTSConfig table
                    database.execSQL(
                        """
                        CREATE TABLE ttsconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT,
                            baseUrl TEXT,
                            apiToken TEXT,
                            modelName TEXT
                        )
                        """.trimIndent()
                    )

                    // AIDictionaryConfig table
                    database.execSQL(
                        """
                        CREATE TABLE aidictionaryconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            dictionaryName TEXT,
                            llmId INTEGER NOT NULL,
                            prompt TEXT,
                            sourceLanguage TEXT,
                            targetLanguage TEXT,
                            FOREIGN KEY(llmId) REFERENCES llmconfig(id) ON DELETE CASCADE
                        )
                        """.trimIndent()
                    )
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_aidictionaryconfig_llmId ON aidictionaryconfig(llmId)")

                    // AITranslatorConfig table
                    database.execSQL(
                        """
                        CREATE TABLE aitranslatorconfig (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            translatorName TEXT,
                            llmId INTEGER NOT NULL,
                            prompt TEXT,
                            isDefault INTEGER NOT NULL,
                            sourceLanguage TEXT,
                            targetLanguage TEXT,
                            FOREIGN KEY(llmId) REFERENCES llmconfig(id) ON DELETE CASCADE
                        )
                        """.trimIndent()
                    )
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_aitranslatorconfig_llmId ON aitranslatorconfig(llmId)")

                    // AIDictionaryCache table
                    database.execSQL(
                        """
                        CREATE TABLE aidictionarycache (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            hwd TEXT,
                            phrase TEXT,
                            sense TEXT,
                            phonetics TEXT,
                            defEn TEXT,
                            defCn TEXT,
                            example TEXT,
                            llmConfigId INTEGER NOT NULL,
                            timestamp INTEGER NOT NULL,
                            FOREIGN KEY(llmConfigId) REFERENCES llmconfig(id) ON DELETE CASCADE
                        )
                        """.trimIndent()
                    )
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_aidictionarycache_llmConfigId ON aidictionarycache(llmConfigId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_aidictionarycache_hwd ON aidictionarycache(hwd)")

                    // AITranslatorCache table
                    database.execSQL(
                        """
                        CREATE TABLE aitranslatorcache (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            sourceText TEXT,
                            sourceLanguage TEXT,
                            targetLanguage TEXT,
                            translatedText TEXT,
                            llmConfigId INTEGER NOT NULL,
                            timestamp INTEGER NOT NULL,
                            FOREIGN KEY(llmConfigId) REFERENCES llmconfig(id) ON DELETE CASCADE
                        )
                        """.trimIndent()
                    )
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_aitranslatorcache_llmConfigId ON aitranslatorcache(llmConfigId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_aitranslatorcache_sourceText ON aitranslatorcache(sourceText)")

                    // Re-enable foreign keys
                    database.execSQL("PRAGMA foreign_keys=ON")

                    android.util.Log.d("AppDatabase", "Migration 6→7 completed: AI entities now managed by Room")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Migration 6→7 failed", e)
                    throw e
                }
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
