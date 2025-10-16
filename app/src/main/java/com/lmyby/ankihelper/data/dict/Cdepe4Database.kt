package com.lmyby.ankihelper.data.dict

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Database(entities = [DummyEntity::class], version = 2, exportSchema = false)
abstract class Cdepe4Database : RoomDatabase() {

    abstract fun cdepe4Dao(): Cdepe4Dao

    companion object {
        @Volatile
        private var instance: Cdepe4Database? = null

        @JvmStatic
        fun getInstance(context: Context): Cdepe4Database {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    Cdepe4Database::class.java,
                    "cdepe4.db"
                )
                    .createFromAsset("databases/cdepe4.db")
                    .fallbackToDestructiveMigration()
                    // Removed .allowMainThreadQueries() - use coroutines for async operations
                    .build().also { instance = it }
            }
        }
    }
}
