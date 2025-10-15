package com.mmjang.ankihelper.data.ai

import androidx.room.*

@Dao
interface TTSConfigDao {
    @Query("SELECT * FROM ttsconfig ORDER BY id")
    suspend fun getAllTTSConfigs(): List<TTSConfig>

    @Query("SELECT * FROM ttsconfig WHERE id = :id LIMIT 1")
    suspend fun getTTSConfigById(id: Long): TTSConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTTSConfig(config: TTSConfig): Long

    @Update
    suspend fun updateTTSConfig(config: TTSConfig): Int

    @Delete
    suspend fun deleteTTSConfig(config: TTSConfig): Int

    @Query("DELETE FROM ttsconfig WHERE id = :id")
    suspend fun deleteTTSConfigById(id: Long): Int

    @Query("DELETE FROM ttsconfig")
    suspend fun deleteAllTTSConfigs()
}
