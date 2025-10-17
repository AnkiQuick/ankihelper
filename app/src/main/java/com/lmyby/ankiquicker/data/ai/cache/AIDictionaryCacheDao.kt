package com.lmyby.ankiquicker.data.ai.cache

import androidx.room.*

@Dao
interface AIDictionaryCacheDao {
    @Query("SELECT * FROM aidictionarycache WHERE hwd = :headword")
    suspend fun getCacheByHeadword(headword: String): List<AIDictionaryCache>

    @Query("SELECT * FROM aidictionarycache WHERE hwd = :headword AND llmConfigId = :llmConfigId")
    suspend fun getCacheByHeadwordAndLLM(headword: String, llmConfigId: Long): List<AIDictionaryCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: AIDictionaryCache): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaches(caches: List<AIDictionaryCache>)

    @Delete
    suspend fun deleteCache(cache: AIDictionaryCache): Int

    @Query("DELETE FROM aidictionarycache WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldCache(beforeTimestamp: Long): Int

    @Query("DELETE FROM aidictionarycache WHERE id IN (SELECT id FROM aidictionarycache ORDER BY timestamp ASC LIMIT :count)")
    suspend fun deleteOldestCache(count: Int): Int

    @Query("DELETE FROM aidictionarycache")
    suspend fun deleteAllCache()

    @Query("SELECT COUNT(*) FROM aidictionarycache")
    suspend fun getCount(): Int
}
