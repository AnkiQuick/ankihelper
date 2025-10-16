package com.lmyby.ankihelper.data.dict

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Database(entities = [DummyEntity::class], version = 2, exportSchema = false)
abstract class FormsDatabase : RoomDatabase() {

    abstract fun formsDao(): FormsDao

    companion object {
        @Volatile
        private var instance: FormsDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): FormsDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FormsDatabase::class.java,
                    "forms.db"
                )
                    .createFromAsset("databases/forms.db")
                    .fallbackToDestructiveMigration()
                    // Removed .allowMainThreadQueries() - use coroutines for async operations
                    .build().also { instance = it }
            }
        }
    }
}
