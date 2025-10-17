package com.lmyby.ankiquicker.data.plan

import android.content.Context
import com.lmyby.ankiquicker.MyApplication
import com.lmyby.ankiquicker.R
import java.io.IOException

/**
 * Created by liao on 2017/3/19.
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class VocabularyCardModel(ct: Context) {
    private val MODEL_FILE = "vocabulary_card_model.html"
    private val MODEL_SPLITTER = "@@@"
    private val CODING = "UTF-8"
    private val NUMBER_OF_MODEL_STRING = 5

    private val front = Array(2) { "" }
    private var css = ""
    private val back = Array(2) { "" }

    val QFMT = Array(2) { "" }
    val AFMT = Array(2) { "" }
    val Cards = arrayOf("recite", "type")
    var CSS: String = ""

    init {
        try {
            val ips = ct.resources.assets.open(MODEL_FILE)
            val data = ByteArray(ips.available())
            ips.read(data)
            val defaultModelStr = String(data, charset(CODING))
            val defaultModelSplitted = defaultModelStr.split(MODEL_SPLITTER)
            if (defaultModelSplitted.size == NUMBER_OF_MODEL_STRING) {
                front[0] = defaultModelSplitted[0]
                back[0] = defaultModelSplitted[1]
                front[1] = defaultModelSplitted[2]
                back[1] = defaultModelSplitted[3]
                css = defaultModelSplitted[4]
            }
            QFMT[0] = front[0]
            QFMT[1] = front[1]
            AFMT[0] = back[0]
            AFMT[1] = back[1]
            CSS = css
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        // Resource IDs for internationalization
        private val FIELD_RES_IDS = intArrayOf(
            R.string.vocab_field_word,
            R.string.vocab_field_phonetic,
            R.string.vocab_field_definition,
            R.string.vocab_field_notes,
            R.string.vocab_field_example,
            0, // "url" - no translation needed
            R.string.vocab_field_pronunciation
        )

        /**
         * Get localized vocabulary card field names
         * @return Array of localized field names
         */
        @JvmStatic
        fun getFields(): Array<String> {
            val context = MyApplication.getContext()
            return Array(FIELD_RES_IDS.size) { i ->
                if (FIELD_RES_IDS[i] == 0) {
                    // Handle special case for "url" which doesn't need translation
                    "url"
                } else {
                    context.getString(FIELD_RES_IDS[i])
                }
            }
        }

        /**
         * @deprecated Use {@link #getFields()} instead for localized field names
         */
        @Deprecated("Use getFields() instead for localized field names")
        @JvmField
        val FILEDS = arrayOf(
            "单词",
            "音标",
            "释义",
            "笔记",
            "例句",
            "url",
            "发音"
        )
    }
}
