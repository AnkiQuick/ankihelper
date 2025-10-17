package com.lmyby.ankihelper.data.dict

import android.content.Context
import android.database.Cursor
import android.util.Log
import android.widget.FilterQueryProvider
import android.widget.ListAdapter
import android.widget.SimpleCursorAdapter
import com.lmyby.ankihelper.MyApplication
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.data.ai.AIConfigRepository
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Oxford Advanced Learner's Dictionary (10th Edition)
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class Oalde10(private val mContext: Context) : IDictionary {
    private val roomDb: Oalde10Database = Oalde10Database.getInstance(mContext)
    private val dao: Oalde10Dao = roomDb.oalde10Dao()

    override fun getDictionaryKey(): String = "oaldpe10"

    /**
     * Get export elements list with localized field names
     * @return Array of localized field names
     */
    override fun getExportElementsList(): Array<String> {
        val context = mContext ?: MyApplication.getContext()
        return DICT_FIELD_RES_IDS.map { context.getString(it) }.toTypedArray()
    }

    override fun getDictionaryName(): String {
        val context = mContext ?: MyApplication.getContext()
        return context.getString(DICT_NAME_RES_ID)
    }

    override fun getIntroduction(): String {
        val context = mContext ?: MyApplication.getContext()
        return context.getString(DICT_DESC_RES_ID)
    }

    override fun wordLookup(key: String): List<Definition> {
        val cleanedKey = keyCleanup(key)
        val re = queryDefinition(cleanedKey).toMutableList()
        Log.d("", "单词需要查找变形表")
        val deflectResult = getForms(cleanedKey)
        for (s in deflectResult) {
            Log.d("", "已变形单词$s")
        }
        if (deflectResult.isNotEmpty()) {
            for (deflectedWord in deflectResult) {
                re.addAll(queryDefinition(deflectedWord))
            }
        }

        if (re.isEmpty()) {
            // Try to use AI dictionary as fallback
            try {
                val aiConfigs = AIConfigRepository.getAllAIDictionaryConfigs()
                if (aiConfigs.isNotEmpty()) {
                    val aiDict = AIDictionary(aiConfigs[0]) // Use the first AI dictionary config
                    re.addAll(aiDict.wordLookup(key))
                }
            } catch (e: Exception) {
                // If AI lookup fails, continue with empty result
            }
        }

        return re
    }

    /**
     * @param context this
     * @param layout  support_simple_spinner_dropdown_item
     * @return
     */
    override fun getAutoCompleteAdapter(context: Context, layout: Int): ListAdapter {
        val adapter = SimpleCursorAdapter(
            context, layout,
            null,
            arrayOf(FIELD_HWD),
            intArrayOf(android.R.id.text1),
            0
        )
        adapter.filterQueryProvider = FilterQueryProvider { constraint ->
            getFilterCursor(constraint.toString())
        }
        adapter.cursorToStringConverter = SimpleCursorAdapter.CursorToStringConverter { cursor ->
            cursor.getString(1)
        }
        return adapter
    }

    /**
     * @param q word to lookup
     * @return a array of definitions, return ArrayList<>() if none was found
     */
    private fun queryDefinition(q: String): ArrayList<Definition> {
        val re = ArrayList<Definition>()
        if (q.isEmpty()) {
            return re
        }
        // Use Room DAO (blocking call)
        dao.queryDefinition(q).use { cursor ->
            while (cursor.moveToNext()) {
                val def = getDefFromCursor(cursor)
                re.add(def)
            }
        }
        return re
    }

    private fun getDefFromCursor(cursor: Cursor): Definition {
        val eleMap = HashMap<String, String>()
        val fieldNames = getExportElementsList() // Get localized field names

        val hwd = cursor.getString(0)
        val phrase = cursor.getString(1).trim()
        val sense = cursor.getString(2).trim()
        val phonetics = cursor.getString(3).trim()
        val defEn = cursor.getString(4).trim()
        val defCn = cursor.getString(5).trim()

        eleMap[fieldNames[0]] = hwd
        eleMap[fieldNames[1]] =
            "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense</span>"
        eleMap[fieldNames[2]] = "<span >$phonetics</span>"

        if (phrase.isEmpty()) {
            eleMap[fieldNames[3]] =
                "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defEn</span>"

            eleMap[fieldNames[4]] =
                "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defCn</span>"
        } else {
            eleMap[fieldNames[3]] =
                "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$phrase </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defEn</span>"

            eleMap[fieldNames[4]] =
                "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$phrase </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defCn</span>"
        }

        eleMap[fieldNames[5]] = getYoudaoAudioTag(hwd, 2)
        eleMap[fieldNames[6]] = getYoudaoAudioTag(hwd, 1)

        val displayHtml = buildString {
            if (phrase.isEmpty()) {
                append("<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense </span>&nbsp; <span> $phonetics</span> <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defEn</span> <span sytle=margin-right:3px; padding:0;margin:0; padding:0;>$defCn</span>")
            } else {
                append("<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$phrase </span>&nbsp; </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense </span>&nbsp; <span> $phonetics</span> <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defEn</span> <span sytle=margin-right:3px; padding:0;margin:0; padding:0;>$defCn</span>")
            }
        }

        return Definition(eleMap, displayHtml)
    }

    private fun getForms(q: String): Array<String> {
        // Use Room DAO (blocking call)
        dao.getForms(q.lowercase()).use { cursor ->
            var bases = ""
            while (cursor.moveToNext()) {
                bases = cursor.getString(0)
            }
            return bases.split("@@@").toTypedArray()
        }
    }

    private fun getFilterCursor(q: String): Cursor {
        Log.d("databse", "getFilterCursor$q")
        // Use Room DAO (blocking call)
        return dao.getFilterCursor("$q%")
    }

    /**
     * 去除左右两边空格，标点
     */
    private fun keyCleanup(key: String): String {
        return key.trim().replace(Regex("""[,.!?()"'""'？]"""), "").lowercase()
    }

    private fun toDefinition(word: String, phonetic: String, definitionHtml: String): Definition {
        val fieldNames = getExportElementsList() // Get localized field names
        val exp = mapOf(
            fieldNames[0] to word,
            fieldNames[1] to phonetic,
            fieldNames[2] to definitionHtml,
            fieldNames[3] to getYoudaoAudioTag(word, 2),
            fieldNames[4] to getYoudaoAudioTag(word, 1)
        )
        return Definition(exp, definitionHtml)
    }

    private fun getYoudaoAudioTag(word: String, voiceType: Int): String {
        return "[sound:https://dict.youdao.com/dictvoice?audio=$word&type=$voiceType]"
    }

    private fun decodeHtmlContent(encodedText: String): String? {
        return try {
            URLDecoder.decode(encodedText, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val DATABASE_NAME = "oaldpe10.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_DICT = "dict"
        private const val FIELD_HWD = "hwd"
        private const val FIELD_PHRASE = "phrase"
        private const val FIELD_SENSE = "sense"
        private const val FIELD_PHONETICS = "phonetics"
        private const val FIELD_DEF_EN = "def_en"
        private const val FIELD_DEF_CN = "def_cn"

        // Resource IDs for internationalization
        private val DICT_NAME_RES_ID = R.string.dict_name_oxford
        private val DICT_DESC_RES_ID = R.string.dict_desc_oxford
        private val DICT_FIELD_RES_IDS = intArrayOf(
            R.string.dict_field_word,
            R.string.dict_field_pos,
            R.string.dict_field_phonetic,
            R.string.dict_field_en_definition,
            R.string.dict_field_zh_definition,
            R.string.dict_field_us_pronunciation,
            R.string.dict_field_uk_pronunciation
        )
    }
}
