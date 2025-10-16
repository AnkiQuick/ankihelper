package com.lmyby.ankihelper.data.plan

import com.lmyby.ankihelper.util.Utils

/**
 * Created by liao on 2017/4/20.
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class OutputPlan {
    var planName: String? = null
    var dictionaryKey: String? = null
    var outputDeckId: Long = 0
    var outputModelId: Long = 0
    private var fieldsMapString: String? = null

    var fieldsMap: Map<String, String>
        get() = Utils.fieldsStr2Map(fieldsMapString ?: "")
        set(value) {
            fieldsMapString = Utils.fieldsMap2Str(value)
        }

    fun getFieldsMapString(): String? {
        return fieldsMapString
    }
}
