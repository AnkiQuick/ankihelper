package com.lmyby.ankihelper.data.quote

import android.content.Context
import kotlin.random.Random

/**
 * Created by liao on 2017/8/13.
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class QuoteDb private constructor(context: Context) {
    private val db: QuoteDatabaseHelper = QuoteDatabaseHelper(context)
    private val mContext: Context = context

    fun getQuote(): String {
        val randomKey = randInt(0, ID_MAX)
        val database = db.readableDatabase
        val cursor = database.query(
            "quote",
            arrayOf("content"),
            "id=?",
            arrayOf(randomKey.toString()),
            null,
            null,
            null
        )
        var content = ""
        if (cursor.moveToNext()) {
            content = cursor.getString(0)
        }
        cursor.close()
        return content
    }

    companion object {
        private const val DATABASE_NAME = "quote.db"
        private const val DATABASE_VERSION = 1
        private const val ID_MAX = 15972

        @Volatile
        private var instance: QuoteDb? = null

        @JvmStatic
        fun getInstance(context: Context): QuoteDb {
            return instance ?: synchronized(this) {
                instance ?: QuoteDb(context).also { instance = it }
            }
        }

        @JvmStatic
        fun randInt(min: Int, max: Int): Int {
            return Random.nextInt(min, max + 1)
        }
    }
}
