package com.lmyby.ankihelper.data.ai

import androidx.room.*

@Dao
interface AITranslatorConfigDao {
    @Query("SELECT * FROM aitranslatorconfig ORDER BY id")
    suspend fun getAllAITranslatorConfigs(): List<AITranslatorConfig>

    @Query("SELECT * FROM aitranslatorconfig WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultAITranslatorConfig(): AITranslatorConfig?

    @Query("SELECT * FROM aitranslatorconfig WHERE id = :id LIMIT 1")
    suspend fun getAITranslatorConfigById(id: Long): AITranslatorConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAITranslatorConfig(config: AITranslatorConfig): Long

    @Update
    suspend fun updateAITranslatorConfig(config: AITranslatorConfig): Int

    @Delete
    suspend fun deleteAITranslatorConfig(config: AITranslatorConfig): Int

    @Query("DELETE FROM aitranslatorconfig WHERE id = :id")
    suspend fun deleteAITranslatorConfigById(id: Long): Int

    @Query("UPDATE aitranslatorconfig SET isDefault = 0 WHERE isDefault = 1")
    suspend fun clearAllDefaults()

    @Query("DELETE FROM aitranslatorconfig")
    suspend fun deleteAllAITranslatorConfigs()
}
