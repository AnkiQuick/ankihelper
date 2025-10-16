package com.lmyby.ankihelper.data.history

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
data class HistoryPOJO(
    var timeStamp: Long = 0,
    var type: Int = 0,
    var word: String = "",
    var sentence: String = "",
    var dictionary: String = "",
    var definition: String = "",
    var translation: String = "",
    var note: String = "",
    var tag: String = ""
)
