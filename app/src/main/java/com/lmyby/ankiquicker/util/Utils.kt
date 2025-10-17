package com.lmyby.ankiquicker.util

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.res.TypedArray
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.ichi2.anki.FlashCardsContract
import com.lmyby.ankiquicker.data.dict.Definition
import com.lmyby.ankiquicker.data.plan.OutputPlanPOJO
import kotlin.random.Random

/**
 * Created by liao on 2017/4/27.
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object Utils {
    private const val FIELDS_SEPERATOR = "@@@@"

    @JvmStatic
    fun fieldsMap2Str(map: Map<String, String>): String {
        val sb = StringBuilder()
        for ((key, value) in map) {
            sb.append(key)
            sb.append(FIELDS_SEPERATOR)
            sb.append(value)
            sb.append(FIELDS_SEPERATOR)
        }
        return sb.toString()
    }

    @JvmStatic
    fun fieldsStr2Map(str: String): Map<String, String> {
        val results = LinkedHashMap<String, String>()
        val fields = str.split(FIELDS_SEPERATOR)
        val pairs = fields.size / 2
        for (i in 0 until pairs) {
            results[fields[i * 2]] = fields[i * 2 + 1]
        }
        return results
    }

    @JvmStatic
    fun hashMap2LinkedHashMap(hashMap: Map<Long, String>): LinkedHashMap<Long, String> {
        val linkedHashMap = LinkedHashMap<Long, String>()
        val keyArray = hashMap.keys.toTypedArray()
        keyArray.sort()
        for (k in hashMap.keys) {
            linkedHashMap[k] = hashMap[k]!!
        }
        return linkedHashMap
    }

    @JvmStatic
    fun getMapKeyArray(map: Map<Long, String>): LongArray {
        val keyArr = LongArray(map.size)
        var i = 0
        for (id in map.keys) {
            keyArr[i] = id
            i++
        }
        return keyArr
    }

    @JvmStatic
    fun getMapValueArray(map: Map<Long, String>): Array<String> {
        val valArr = Array(map.size) { "" }
        var i = 0
        for (value in map.values) {
            valArr[i] = value
            i++
        }
        return valArr
    }

    @JvmStatic
    fun findMapKeyByVal(map: Map<Long, String>, value: String): Long {
        for ((key, mapValue) in map) {
            if (mapValue == value)
                return key
        }
        return -1
    }

    @JvmStatic
    fun <T> concatenate(a: Array<T>, b: Array<T>): Array<T> {
        return a + b
    }

    @JvmStatic
    fun getPX(context: Context, dp: Int): Int {
        // margin in dips
        // int dpValue = 5; // margin in dips
        val d = context.resources.displayMetrics.density
        return (dp * d).toInt()
    }

    @JvmStatic
    fun hideSoftKeyboard(context: Activity) {
        // Hides the SoftKeyboard
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = context.currentFocus
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    @JvmStatic
    fun getArrayIndex(arr: LongArray, value: Long): Int {
        for (i in arr.indices) {
            if (arr[i] == value) return i
        }
        return -1
    }

    @JvmStatic
    fun getAllHtmlFromDefinitionList(defList: List<Definition>): String {
        if (defList.size <= 1) {
            return ""
        }
        val sb = StringBuilder()
        sb.append("<div>")
        for (def in defList) {
            sb.append("<div>")
            sb.append(def.displayHtml)
            sb.append("</div>")
            sb.append("<br/>")
        }
        sb.append("</div>")
        return sb.toString()
    }

    @JvmStatic
    fun containsTranslationField(outputPlan: OutputPlanPOJO?): Boolean {
        if (outputPlan == null) {
            return false
        }
        val map = outputPlan.fieldsMap
        for (key in map.keys) {
            if (map[key] == "句子翻译") {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun getResIdFromAttribute(activity: Activity, attr: Int): Int {
        val a: TypedArray = activity.theme.obtainStyledAttributes(
            activity.applicationInfo.theme, intArrayOf(attr)
        )
        val attributeResourceId = a.getResourceId(0, 0)
        a.recycle()
        return attributeResourceId
    }

    @JvmStatic
    fun deleteNote(context: Context, noteid: Long): Boolean {
        val cr = context.contentResolver ?: return false
        val noteUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, noteid.toString())
        val i = cr.delete(noteUri, null, null)
        return i == 1
    }

    @JvmStatic
    fun renderTmpl(tmpl: String, dataMap: Map<String, String>): String {
        var result = tmpl.trim()
        for ((key, value) in dataMap) {
            result = result.replace("{{$key}}", value)
        }
        return result
    }

    @JvmStatic
    fun keyCleanup(key: String): String {
        return key.trim().replace(Regex("""[,.!?()\"'""'？]"""), "").lowercase()
    }

    @JvmStatic
    fun showMessage(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes) { _, _ -> }
            .show()
    }

    @JvmStatic
    fun fromTagSetToString(tagSet: Set<String>): String {
        return tagSet.joinToString(",") { it.trim() }
    }

    @JvmStatic
    fun fromStringToTagSet(s: String): Set<String> {
        return s.split(",").toSet()
    }

    @JvmStatic
    fun getRandomHexString(numchars: Int): String {
        val sb = StringBuilder()
        while (sb.length < numchars) {
            sb.append(Random.nextInt().toString(16))
        }
        return sb.toString().substring(0, numchars)
    }
}
