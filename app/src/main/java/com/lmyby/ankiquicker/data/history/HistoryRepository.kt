package com.lmyby.ankiquicker.data.history

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for History operations
 *
 * Provides an abstraction layer for history-related database operations,
 * including querying, inserting, and managing word lookup history.
 */
class HistoryRepository(private val historyDao: HistoryDao) {

    /**
     * Get all history entries, sorted by timestamp (newest first)
     * @return List of all HistoryEntity objects
     */
    suspend fun getAllHistory(): List<HistoryEntity> = withContext(Dispatchers.IO) {
        historyDao.getAllHistory()
    }

    /**
     * Get history entries after a specific timestamp
     * Useful for incremental loading or sync operations
     * @param afterTimestamp The timestamp to query after
     * @return List of HistoryEntity objects newer than the given timestamp
     */
    suspend fun getHistoryAfter(afterTimestamp: Long): List<HistoryEntity> = withContext(Dispatchers.IO) {
        historyDao.getHistoryAfter(afterTimestamp)
    }

    /**
     * Get history entries before a specific timestamp with a limit
     * Used for pagination - loading older history in chunks
     * @param beforeTimestamp The timestamp to query before
     * @param limit Maximum number of entries to return
     * @return List of HistoryEntity objects older than the given timestamp
     */
    suspend fun getHistoryBefore(beforeTimestamp: Long, limit: Int = 50): List<HistoryEntity> = withContext(Dispatchers.IO) {
        historyDao.getHistoryBefore(beforeTimestamp, limit)
    }

    /**
     * Get a specific history entry by timestamp
     * @param timestamp The timestamp of the history entry
     * @return The HistoryEntity if found, null otherwise
     */
    suspend fun getHistoryByTimestamp(timestamp: Long): HistoryEntity? = withContext(Dispatchers.IO) {
        historyDao.getHistoryByTimestamp(timestamp)
    }

    /**
     * Search history by word
     * @param word The word to search for (exact match)
     * @return List of HistoryEntity objects matching the word
     */
    suspend fun searchByWord(word: String): List<HistoryEntity> = withContext(Dispatchers.IO) {
        historyDao.searchByWord(word)
    }

    /**
     * Search history by partial word match
     * @param query The search query (will match words containing this string)
     * @return List of HistoryEntity objects with words containing the query
     */
    suspend fun searchByWordPartial(query: String): List<HistoryEntity> = withContext(Dispatchers.IO) {
        historyDao.searchByWordPartial(query)
    }

    /**
     * Get history entries by type
     * @param type The history type (0=word, 1=sentence, etc.)
     * @return List of HistoryEntity objects of the specified type
     */
    suspend fun getHistoryByType(type: Int): List<HistoryEntity> = withContext(Dispatchers.IO) {
        historyDao.getHistoryByType(type)
    }

    /**
     * Get history entries from a specific dictionary
     * @param dictionary The dictionary key
     * @return List of HistoryEntity objects from the specified dictionary
     */
    suspend fun getHistoryByDictionary(dictionary: String): List<HistoryEntity> = withContext(Dispatchers.IO) {
        historyDao.getHistoryByDictionary(dictionary)
    }

    /**
     * Get history entries with a specific tag
     * @param tag The tag to filter by
     * @return List of HistoryEntity objects with the specified tag
     */
    suspend fun getHistoryByTag(tag: String): List<HistoryEntity> = withContext(Dispatchers.IO) {
        historyDao.getHistoryByTag(tag)
    }

    /**
     * Insert a single history entry
     * @param history The HistoryEntity to insert
     * @return The row ID of the inserted history
     */
    suspend fun insertHistory(history: HistoryEntity): Long = withContext(Dispatchers.IO) {
        historyDao.insertHistory(history)
    }

    /**
     * Insert multiple history entries
     * Uses REPLACE conflict strategy, so duplicates will be updated
     * @param histories List of HistoryEntity objects to insert
     */
    suspend fun insertHistories(histories: List<HistoryEntity>) = withContext(Dispatchers.IO) {
        historyDao.insertHistories(histories)
    }

    /**
     * Update a history entry
     * @param history The HistoryEntity with updated data
     * @return The number of rows updated
     */
    suspend fun updateHistory(history: HistoryEntity): Int = withContext(Dispatchers.IO) {
        historyDao.updateHistory(history)
    }

    /**
     * Delete a history entry by timestamp
     * @param timestamp The timestamp of the history entry to delete
     * @return The number of rows deleted
     */
    suspend fun deleteHistory(timestamp: Long): Int = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryByTimestamp(timestamp)
    }

    /**
     * Delete all history entries
     * @return The number of rows deleted
     */
    suspend fun deleteAllHistory(): Int = withContext(Dispatchers.IO) {
        historyDao.deleteAllHistory()
    }

    /**
     * Get the count of all history entries
     * @return The number of history entries in the database
     */
    suspend fun getHistoryCount(): Int = withContext(Dispatchers.IO) {
        historyDao.getHistoryCount()
    }

    /**
     * Get the most recent history entry
     * @return The most recent HistoryEntity, or null if no history exists
     */
    suspend fun getMostRecentHistory(): HistoryEntity? = withContext(Dispatchers.IO) {
        historyDao.getMostRecentHistory()
    }
}
