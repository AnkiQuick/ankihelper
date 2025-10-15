package com.lmyby.ankihelper.data.history

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper class to provide simpler access to HistoryRepository
 *
 * This class wraps the suspend functions in the repository for use in utility
 * classes and Java code that don't have access to lifecycle scope.
 */
class HistoryRepositoryHelper(private val repository: HistoryRepository) {

    /**
     * Insert a history entry in the background
     * Fire-and-forget operation
     */
    fun insertHistory(history: HistoryEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.insertHistory(history)
            } catch (e: Exception) {
                // Log error but don't propagate for fire-and-forget operations
                android.util.Log.e("HistoryRepoHelper", "Error inserting history", e)
            }
        }
    }

    /**
     * Insert multiple history entries in the background
     * Fire-and-forget operation
     */
    fun insertHistories(histories: List<HistoryEntity>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.insertHistories(histories)
            } catch (e: Exception) {
                android.util.Log.e("HistoryRepoHelper", "Error inserting histories", e)
            }
        }
    }

    /**
     * Delete a history entry in the background
     * Fire-and-forget operation
     */
    fun deleteHistory(timestamp: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.deleteHistory(timestamp)
            } catch (e: Exception) {
                android.util.Log.e("HistoryRepoHelper", "Error deleting history", e)
            }
        }
    }
}
