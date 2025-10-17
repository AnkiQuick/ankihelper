package com.lmyby.ankiquicker.data.dict

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Entity(tableName = "dict")
data class Oalde10Entry(
    @PrimaryKey
    var hwd: String = "",
    var phrase: String? = null,
    var sense: String? = null,
    var phonetics: String? = null,
    var defEn: String? = null,
    var defCn: String? = null
)
