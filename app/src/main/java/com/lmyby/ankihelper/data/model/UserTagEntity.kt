package com.lmyby.ankihelper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Room entity for UserTag
 * Replaces UserTag (LitePal) model
 *
 * Table: usertag
 * Primary key: tag (unique tag identifier)
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Entity(tableName = "usertag")
data class UserTagEntity(
    @PrimaryKey
    @ColumnInfo(name = "tag")
    var tag: String = ""
) {
    // Constructor for creating with tag
    @Ignore
    constructor(tag: String, dummy: Boolean) : this(tag)
}
