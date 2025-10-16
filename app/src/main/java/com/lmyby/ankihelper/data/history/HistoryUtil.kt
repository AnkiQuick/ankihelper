package com.lmyby.ankihelper.data.history

import android.content.Context
import com.lmyby.ankihelper.data.database.AppDatabase

/**
 * Utility class for saving history entries using Room database
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object HistoryUtil {
    private var repositoryHelper: HistoryRepositoryHelper? = null

    /**
     * Initialize the utility with application context
     * Must be called before any save methods
     */
    @JvmStatic
    fun initialize(context: Context) {
        if (repositoryHelper == null) {
            val database = AppDatabase.getInstance(context.applicationContext)
            val repository = HistoryRepository(database.historyDao())
            repositoryHelper = HistoryRepositoryHelper(repository)
        }
    }

    /**
     * Convert HistoryPOJO to HistoryEntity
     */
    private fun convertToEntity(pojo: HistoryPOJO): HistoryEntity {
        return HistoryEntity(
            timeStamp = pojo.timeStamp,
            type = pojo.type,
            word = pojo.word,
            sentence = pojo.sentence,
            dictionary = pojo.dictionary,
            definition = pojo.definition,
            translation = pojo.translation,
            note = pojo.note,
            tag = pojo.tag
        )
    }

    @JvmStatic
    fun savePopupOpen(sentence: String) {
        if (repositoryHelper == null) {
            android.util.Log.e("HistoryUtil", "HistoryUtil not initialized! Call initialize() first.")
            return
        }
        val history = HistoryPOJO(
            type = HistoryType.POPUP_OPEN,
            timeStamp = System.currentTimeMillis(),
            sentence = sentence
        )
        repositoryHelper!!.insertHistory(convertToEntity(history))
    }

    @JvmStatic
    fun saveWordlookup(sentence: String, word: String) {
        if (repositoryHelper == null) {
            android.util.Log.e("HistoryUtil", "HistoryUtil not initialized! Call initialize() first.")
            return
        }
        val history = HistoryPOJO(
            type = HistoryType.WORD_LOOK_UP,
            timeStamp = System.currentTimeMillis(),
            sentence = sentence,
            word = word
        )
        repositoryHelper!!.insertHistory(convertToEntity(history))
    }

    @JvmStatic
    fun saveNoteAdd(
        sentence: String,
        word: String,
        dictionary: String,
        definition: String,
        translation: String,
        note: String,
        tag: String
    ) {
        if (repositoryHelper == null) {
            android.util.Log.e("HistoryUtil", "HistoryUtil not initialized! Call initialize() first.")
            return
        }
        val history = HistoryPOJO(
            type = HistoryType.NOTE_ADD,
            timeStamp = System.currentTimeMillis(),
            sentence = sentence,
            word = word,
            dictionary = dictionary,
            definition = definition,
            translation = translation,
            note = note,
            tag = tag
        )
        repositoryHelper!!.insertHistory(convertToEntity(history))
    }
}
