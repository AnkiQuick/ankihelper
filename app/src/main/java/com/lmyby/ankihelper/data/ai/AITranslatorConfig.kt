package com.lmyby.ankihelper.data.ai

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Entity(
    tableName = "aitranslatorconfig",
    foreignKeys = [ForeignKey(
        entity = LLMConfig::class,
        parentColumns = ["id"],
        childColumns = ["llmId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("llmId")]
)
data class AITranslatorConfig(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "translatorName")
    var translatorName: String? = null,

    @ColumnInfo(name = "llmId")
    var llmId: Long = 0, // Foreign key to LLMConfig

    var prompt: String? = null,

    @ColumnInfo(name = "isDefault")
    var isDefault: Boolean = false,

    @ColumnInfo(name = "sourceLanguage")
    var sourceLanguage: String? = null,

    @ColumnInfo(name = "targetLanguage")
    var targetLanguage: String? = null
)
