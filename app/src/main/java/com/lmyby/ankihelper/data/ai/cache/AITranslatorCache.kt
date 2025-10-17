package com.lmyby.ankihelper.data.ai.cache

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lmyby.ankihelper.data.ai.LLMConfig

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Entity(
    tableName = "aitranslatorcache",
    foreignKeys = [
        ForeignKey(
            entity = LLMConfig::class,
            parentColumns = ["id"],
            childColumns = ["llmConfigId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("llmConfigId"), Index("sourceText")]
)
data class AITranslatorCache(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "sourceText")
    var sourceText: String = "", // The text to be translated

    @ColumnInfo(name = "sourceLanguage")
    var sourceLanguage: String = "",

    @ColumnInfo(name = "targetLanguage")
    var targetLanguage: String = "",

    @ColumnInfo(name = "translatedText")
    var translatedText: String = "", // The translated result

    @ColumnInfo(name = "llmConfigId")
    var llmConfigId: Long = 0, // Foreign key to LLMConfig

    var timestamp: Long = 0 // For expiration
)
