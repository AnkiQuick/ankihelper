package com.lmyby.ankihelper.data.history

import androidx.room.*

/**
 * DAO for HistoryEntity
 * Provides all CRUD operations for history entries using suspend functions
 */
@Dao
interface HistoryDao {

    /**
     * Get all history entries sorted by timestamp descending (newest first)
     * @return List of all history entries
     */
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<HistoryEntity>

    /**
     * Get history entries after a specific timestamp
     * @param afterTimestamp Get entries after this timestamp
     * @return List of history entries
     */
    @Query("SELECT * FROM history WHERE timestamp > :afterTimestamp ORDER BY timestamp DESC")
    suspend fun getHistoryAfter(afterTimestamp: Long): List<HistoryEntity>

    /**
     * Get history entries before a specific timestamp (for pagination)
     * @param beforeTimestamp Get entries before this timestamp
     * @param limit Maximum number of entries to return
     * @return List of history entries
     */
    @Query("SELECT * FROM history WHERE timestamp < :beforeTimestamp ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getHistoryBefore(beforeTimestamp: Long, limit: Int): List<HistoryEntity>

    /**
     * Get history entries by word (exact match)
     * @param word The word to search for
     * @return List of history entries containing the word
     */
    @Query("SELECT * FROM history WHERE word = :word ORDER BY timestamp DESC")
    suspend fun searchByWord(word: String): List<HistoryEntity>

    /**
     * Search history entries by partial word match
     * @param query The search query
     * @return List of history entries with words containing the query
     */
    @Query("SELECT * FROM history WHERE word LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchByWordPartial(query: String): List<HistoryEntity>

    /**
     * Get history entries from a specific dictionary
     * @param dictionary The dictionary key
     * @return List of history entries from the dictionary
     */
    @Query("SELECT * FROM history WHERE dictionary = :dictionary ORDER BY timestamp DESC")
    suspend fun getHistoryByDictionary(dictionary: String): List<HistoryEntity>

    /**
     * Get history entries by tag
     * @param tag The tag to filter by
     * @return List of history entries with the specified tag
     */
    @Query("SELECT * FROM history WHERE tag = :tag ORDER BY timestamp DESC")
    suspend fun getHistoryByTag(tag: String): List<HistoryEntity>

    /**
     * Get history entries by type
     * @param type The history type
     * @return List of history entries of the specified type
     */
    @Query("SELECT * FROM history WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getHistoryByType(type: Int): List<HistoryEntity>

    /**
     * Get a specific history entry by timestamp
     * @param timestamp The timestamp of the history entry
     * @return The history entry or null if not found
     */
    @Query("SELECT * FROM history WHERE timestamp = :timestamp")
    suspend fun getHistoryByTimestamp(timestamp: Long): HistoryEntity?

    /**
     * Insert a new history entry
     * @param history The history entry to insert
     * @return The row ID of the inserted entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    /**
     * Insert multiple history entries
     * @param histories The history entries to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(histories: List<HistoryEntity>)

    /**
     * Update an existing history entry
     * @param history The history entry to update
     * @return Number of rows updated
     */
    @Update
    suspend fun updateHistory(history: HistoryEntity): Int

    /**
     * Delete a history entry
     * @param history The history entry to delete
     * @return Number of rows deleted
     */
    @Delete
    suspend fun deleteHistory(history: HistoryEntity): Int

    /**
     * Delete a history entry by timestamp
     * @param timestamp The timestamp of the entry to delete
     * @return Number of rows deleted
     */
    @Query("DELETE FROM history WHERE timestamp = :timestamp")
    suspend fun deleteHistoryByTimestamp(timestamp: Long): Int

    /**
     * Delete all history entries
     * @return Number of rows deleted
     */
    @Query("DELETE FROM history")
    suspend fun deleteAllHistory(): Int

    /**
     * Delete history entries older than a specific timestamp
     * @param beforeTimestamp Delete entries before this timestamp
     * @return Number of rows deleted
     */
    @Query("DELETE FROM history WHERE timestamp < :beforeTimestamp")
    suspend fun deleteHistoryBefore(beforeTimestamp: Long): Int

    /**
     * Get count of all history entries
     * @return Number of history entries
     */
    @Query("SELECT COUNT(*) FROM history")
    suspend fun getHistoryCount(): Int

    /**
     * Get the most recent history entry
     * @return The most recent history entry or null if empty
     */
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecentHistory(): HistoryEntity?
}
