package com.lmyby.ankiquicker.data.plan

import com.lmyby.ankiquicker.util.Utils

/**
 * Created by liao on 2017/4/20.
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class OutputPlanPOJO {
    var planName: String = ""
    var dictionaryKey: String = ""
    var outputDeckId: Long = 0
    var outputModelId: Long = 0
    private var fieldsMapString: String = ""

    var fieldsMap: Map<String, String>
        get() = Utils.fieldsStr2Map(fieldsMapString)
        set(value) {
            fieldsMapString = Utils.fieldsMap2Str(value)
        }

    fun getFieldsMapString(): String {
        return fieldsMapString
    }

    fun setFieldsMapString(s: String) {
        this.fieldsMapString = s
    }
}
