package com.lmyby.ankiquicker.data.ai

import androidx.room.*

@Dao
interface LLMConfigDao {
    @Query("SELECT * FROM llmconfig ORDER BY id")
    suspend fun getAllLLMConfigs(): List<LLMConfig>

    @Query("SELECT * FROM llmconfig WHERE id = :id LIMIT 1")
    suspend fun getLLMConfigById(id: Long): LLMConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLLMConfig(config: LLMConfig): Long

    @Update
    suspend fun updateLLMConfig(config: LLMConfig): Int

    @Delete
    suspend fun deleteLLMConfig(config: LLMConfig): Int

    @Query("DELETE FROM llmconfig WHERE id = :id")
    suspend fun deleteLLMConfigById(id: Long): Int

    @Query("DELETE FROM llmconfig")
    suspend fun deleteAllLLMConfigs()
}
