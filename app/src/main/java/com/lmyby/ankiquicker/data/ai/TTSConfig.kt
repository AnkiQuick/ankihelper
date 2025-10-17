package com.lmyby.ankiquicker.data.ai

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Entity(tableName = "ttsconfig")
data class TTSConfig(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String? = null,
    var baseUrl: String? = null,
    var apiToken: String? = null, // Encrypted
    var modelName: String? = null
)
