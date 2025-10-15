package com.lmyby.ankihelper.data.ai

import androidx.room.*

@Dao
interface AIDictionaryConfigDao {
    @Query("SELECT * FROM aidictionaryconfig ORDER BY id")
    suspend fun getAllAIDictionaryConfigs(): List<AIDictionaryConfig>

    @Query("SELECT * FROM aidictionaryconfig WHERE id = :id LIMIT 1")
    suspend fun getAIDictionaryConfigById(id: Long): AIDictionaryConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIDictionaryConfig(config: AIDictionaryConfig): Long

    @Update
    suspend fun updateAIDictionaryConfig(config: AIDictionaryConfig): Int

    @Delete
    suspend fun deleteAIDictionaryConfig(config: AIDictionaryConfig): Int

    @Query("DELETE FROM aidictionaryconfig WHERE id = :id")
    suspend fun deleteAIDictionaryConfigById(id: Long): Int

    @Query("DELETE FROM aidictionaryconfig")
    suspend fun deleteAllAIDictionaryConfigs()
}
