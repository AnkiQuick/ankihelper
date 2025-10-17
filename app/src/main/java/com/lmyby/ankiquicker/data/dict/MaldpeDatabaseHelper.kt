package com.lmyby.ankiquicker.data.dict

import android.content.Context

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class MaldpeDatabaseHelper(context: Context) : BaseDatabaseHelper(context, DATABASE_NAME, 1) {
    companion object {
        private const val DATABASE_NAME = "maldpe.db"
    }
}
