package com.lmyby.ankihelper.data.ai.cache

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lmyby.ankihelper.data.ai.LLMConfig

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 *
 * Same structure as built-in dictionary tables.
 * Each row represents one definition/meaning, so multiple rows for words with multiple meanings.
 */
@Entity(
    tableName = "aidictionarycache",
    foreignKeys = [
        ForeignKey(
            entity = LLMConfig::class,
            parentColumns = ["id"],
            childColumns = ["llmConfigId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("llmConfigId"), Index("hwd")]
)
data class AIDictionaryCache(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    var hwd: String? = null, // Headword (the word being defined)

    var phrase: String? = null, // Fixed phrase with special meaning or usage (empty for single words)

    var sense: String? = null, // Part of speech (verb, noun, adjective, adverb, etc.) - empty for phrases

    var phonetics: String? = null, // British and American English phonetics (e.g., "UK: /ɡʊd/ US: /ɡʊd/")

    @ColumnInfo(name = "defEn")
    var defEn: String? = null, // English definition

    @ColumnInfo(name = "defCn")
    var defCn: String? = null, // Chinese definition

    var example: String? = null, // Example sentence

    @ColumnInfo(name = "llmConfigId")
    var llmConfigId: Long = 0, // Foreign key to LLMConfig

    var timestamp: Long = 0 // For expiration
)
