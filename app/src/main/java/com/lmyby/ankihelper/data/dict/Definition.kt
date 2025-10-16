package com.lmyby.ankihelper.data.dict

/**
 * Created by liao on 2017/4/20.
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class Definition @JvmOverloads constructor(
    private val exportElements: Map<String, String>,
    val displayHtml: String,
    val imageUrl: String? = null,
    val imageName: String? = null,
    val audioUrl: String? = null,
    val audioName: String? = null
) {
    fun getExportElement(key: String): String? {
        return exportElements[key]
    }

    fun hasElement(key: String): Boolean {
        return exportElements.containsKey(key)
    }
}
