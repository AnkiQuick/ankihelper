package com.lmyby.ankiquicker.data.dict

import android.content.Context
import android.widget.ListAdapter

/**
 * Created by liao on 2017/4/13.
 * Converted to Kotlin as part of Phase 2 data model migration
 */
interface IDictionary {
    /**
     * Get a stable, language-independent key for this dictionary
     * This key is used to identify the dictionary in plans and should never change
     * @return A unique, stable identifier for this dictionary
     */
    fun getDictionaryKey(): String

    /**
     * Get the localized display name of this dictionary
     * @return The dictionary name in the current app language
     */
    fun getDictionaryName(): String

    fun getIntroduction(): String

    fun getExportElementsList(): Array<String>

    fun wordLookup(key: String): List<Definition>

    fun getAutoCompleteAdapter(context: Context, layout: Int): ListAdapter
}
