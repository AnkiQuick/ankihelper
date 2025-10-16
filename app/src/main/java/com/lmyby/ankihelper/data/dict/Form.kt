package com.lmyby.ankihelper.data.dict

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Entity(tableName = "forms")
data class Form(
    @PrimaryKey
    var hwd: String = "",
    var bases: String? = null
)
