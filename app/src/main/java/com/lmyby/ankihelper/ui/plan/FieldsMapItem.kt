package com.lmyby.ankihelper.ui.plan

/**
 * Created by liao on 2017/4/28.
 * Converted to Kotlin as part of Phase 11 utility migration
 *
 * Note: Kotlin automatically generates getField(), getExportedElementNames(),
 * getSelectedFieldPos() and setSelectedFieldPos() for Java interop
 */
class FieldsMapItem(
    val field: String,
    val exportedElementNames: Array<String>,
    var selectedFieldPos: Int = 0
)
