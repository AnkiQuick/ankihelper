package com.lmyby.ankihelper.data.plan

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.lmyby.ankihelper.util.Utils

/**
 * Room entity for OutputPlan
 * Replaces OutputPlanPOJO and OutputPlan (LitePal) models
 *
 * Table: plan
 * Primary key: planname (unique plan identifier)
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Entity(tableName = "plan")
data class OutputPlanEntity(
    @PrimaryKey
    @ColumnInfo(name = "planname")
    var planName: String = "",

    @ColumnInfo(name = "dictionarykey")
    var dictionaryKey: String? = null,

    @ColumnInfo(name = "outputdeckid")
    var outputDeckId: Long = 0,

    @ColumnInfo(name = "outputmodelid")
    var outputModelId: Long = 0,

    @ColumnInfo(name = "fieldsmap")
    var fieldsMap: String? = null
) {
    // Constructor for creating from existing data
    @Ignore
    constructor(
        planName: String,
        dictionaryKey: String?,
        outputDeckId: Long,
        outputModelId: Long,
        fieldsMapObj: Map<String, String>
    ) : this(
        planName = planName,
        dictionaryKey = dictionaryKey,
        outputDeckId = outputDeckId,
        outputModelId = outputModelId,
        fieldsMap = Utils.fieldsMap2Str(fieldsMapObj)
    )

    // Helper methods for String/Map conversion (keeps existing functionality from POJO)
    fun getFieldsMapString(): String? {
        return fieldsMap
    }

    fun setFieldsMapString(fieldsMap: String?) {
        this.fieldsMap = fieldsMap
    }

    fun getFieldsMapAsMap(): Map<String, String> {
        return Utils.fieldsStr2Map(fieldsMap ?: "")
    }

    fun setFieldsMapFromMap(fieldsMapObj: Map<String, String>) {
        this.fieldsMap = Utils.fieldsMap2Str(fieldsMapObj)
    }
}
