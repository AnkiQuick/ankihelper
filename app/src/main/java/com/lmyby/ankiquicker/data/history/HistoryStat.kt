package com.lmyby.ankiquicker.data.history

import android.content.Context
import android.util.Log
import com.lmyby.ankiquicker.data.database.AppDatabase
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class HistoryStat(context: Context, private val lastDays: Int) {
    private val startOfToday: Long
    private val startOfThisMonth: Long
    private val startOfLastDays: Long
    private val dataOfLastDays: List<HistoryPOJO>

    init {
        startOfToday = LocalDate.now().atStartOfDay(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
        startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay()
            .atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
        startOfLastDays = LocalDate.now().minusDays((lastDays - 1).toLong()).atStartOfDay()
            .atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()

        // Load history using HistoryRepository with blocking pattern
        val database = AppDatabase.getInstance(context.applicationContext)
        val repository = HistoryRepository(database.historyDao())

        dataOfLastDays = try {
            // Use runBlocking to execute suspend function synchronously
            val entities = runBlocking {
                repository.getHistoryAfter(startOfLastDays)
            }

            // Convert entities to POJOs
            convertEntitiesToPOJOs(entities)
        } catch (e: Exception) {
            Log.e("HistoryStat", "Error loading history", e)
            emptyList()
        }
    }

    /**
     * Convert HistoryEntity list to HistoryPOJO list
     */
    private fun convertEntitiesToPOJOs(entities: List<HistoryEntity>): List<HistoryPOJO> {
        return entities.map { entity ->
            HistoryPOJO(
                timeStamp = entity.timeStamp,
                type = entity.type,
                word = entity.word,
                sentence = entity.sentence,
                dictionary = entity.dictionary,
                definition = entity.definition,
                translation = entity.translation,
                note = entity.note,
                tag = entity.tag
            )
        }
    }

    fun getHourStatistics(): Array<IntArray> {
        val result = Array(3) { IntArray(24) }
        for (history in dataOfLastDays) {
            val mills = history.timeStamp
            val type = history.type
            val hour = LocalDateTime.ofInstant(Instant.ofEpochMilli(mills), ZoneId.systemDefault()).hour
            result[type][hour] += 1
        }
        return result
    }

    fun getLastDaysStatistics(): Array<IntArray> {
        val result = Array(3) { IntArray(lastDays) }
        for (history in dataOfLastDays) {
            val pos = ((history.timeStamp - startOfLastDays) / MILLIS_OF_DAY).toInt()
            val type = history.type
            result[type][pos] += 1
        }
        return result
    }

    companion object {
        private const val MILLIS_OF_DAY = 3600 * 24 * 1000L
    }
}
