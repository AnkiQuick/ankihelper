package com.lmyby.ankiquicker.data.dict

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Database(entities = [DummyEntity::class], version = 2, exportSchema = false)
abstract class MaldpeDatabase : RoomDatabase() {

    abstract fun maldpeDao(): MaldpeDao

    companion object {
        @Volatile
        private var instance: MaldpeDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): MaldpeDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MaldpeDatabase::class.java,
                    "maldpe.db"
                )
                    .createFromAsset("databases/maldpe.db")
                    .fallbackToDestructiveMigration()
                    // Removed .allowMainThreadQueries() - use coroutines for async operations
                    .build().also { instance = it }
            }
        }
    }
}
