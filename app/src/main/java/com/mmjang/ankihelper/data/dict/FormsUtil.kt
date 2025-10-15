package com.mmjang.ankihelper.data.dict

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FormsUtil private constructor(context: Context) {
    private val db: FormsDatabase = FormsDatabase.getInstance(context)

    companion object {
        @Volatile
        private var instance: FormsUtil? = null

        fun getInstance(context: Context): FormsUtil {
            return instance ?: synchronized(this) {
                instance ?: FormsUtil(context).also { instance = it }
            }
        }
    }

    /**
     * Get word forms using suspend function for async operation
     * @param q The word to query
     * @return Array of base forms
     */
    suspend fun getForms(q: String): Array<String> = withContext(Dispatchers.IO) {
        try {
            val bases = db.formsDao().getForms(q.lowercase())

            if (bases.isNullOrEmpty()) {
                emptyArray()
            } else {
                bases.split("@@@").toTypedArray()
            }
        } catch (e: Exception) {
            // Log error and return empty array
            android.util.Log.e("FormsUtil", "Error getting forms for '$q'", e)
            emptyArray()
        }
    }

    /**
     * Synchronous wrapper for Java compatibility (uses runBlocking - use sparingly)
     * Prefer using the suspend version directly from Kotlin code
     */
    @Deprecated("Use suspend version instead", ReplaceWith("getForms(q)"))
    fun getFormsSync(q: String): Array<String> {
        return kotlinx.coroutines.runBlocking {
            getForms(q)
        }
    }
}
