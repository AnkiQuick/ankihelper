package com.lmyby.ankiquicker.data.dict

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Database(entities = [DummyEntity::class], version = 2, exportSchema = false)
abstract class Oalde10Database : RoomDatabase() {

    abstract fun oalde10Dao(): Oalde10Dao

    companion object {
        @Volatile
        private var instance: Oalde10Database? = null

        @JvmStatic
        fun getInstance(context: Context): Oalde10Database {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    Oalde10Database::class.java,
                    "oaldpe10.db"
                )
                    .createFromAsset("databases/oaldpe10.db")
                    .fallbackToDestructiveMigration()
                    // Removed .allowMainThreadQueries() - use coroutines for async operations
                    .build().also { instance = it }
            }
        }
    }
}
