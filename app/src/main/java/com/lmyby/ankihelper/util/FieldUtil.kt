package com.lmyby.ankihelper.util

import com.lmyby.ankihelper.ui.widget.BigBangLayout

/**
 * Created by chenxiangjie on 2017/7/26.
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object FieldUtil {

    @JvmStatic
    fun getSelectedText(lines: List<BigBangLayout.Line>): String {
        val sb = StringBuilder()
        val selectedItems = getSelectedItems(lines)
        for (item in selectedItems) {
            if (item.isSelected()) {
                sb.append(item.getText())
                if (RegexUtil.isEnglish(item.getText().toString()) || RegexUtil.isSpecialWord(item.getText().toString())) {
                    sb.append(" ")
                }
            }
        }
        return sb.toString().trim()
    }

    private fun getSelectedItems(lines: List<BigBangLayout.Line>): List<BigBangLayout.Item> {
        val selectedItems = mutableListOf<BigBangLayout.Item>()
        for (line in lines) {
            line.getItems()?.forEach { item ->
                if (item.isSelected()) {
                    selectedItems.add(item)
                }
            }
        }
        return selectedItems
    }

    @JvmStatic
    fun getNormalSentence(lines: List<BigBangLayout.Line>): String {
        val sb = StringBuilder()
        for (line in lines) {
            line.getItems()?.forEach { item ->
                if (item.getText() == "\n") {
                    sb.append("<br/>")
                }
                if (item.isSelected()) {
                    // sb.append("<b>")
                    sb.append(item.getText())
                    // sb.append("</b>")
                } else {
                    sb.append(item.getText())
                }
            }
        }
        return sb.toString().trim()
    }

    @JvmStatic
    fun getBoldSentence(lines: List<BigBangLayout.Line>): String {
        val sb = StringBuilder()
        for (line in lines) {
            line.getItems()?.forEach { item ->
                if (item.getText() == "\n") {
                    sb.append("<br/>")
                }
                if (item.isSelected()) {
                    sb.append("<b>")
                    sb.append(item.getText())
                    sb.append("</b>")
                } else {
                    sb.append(item.getText())
                }
            }
        }
        return sb.toString().trim()
    }

    @JvmStatic
    fun getBlankSentence(lines: List<BigBangLayout.Line>, multiCardMode: Boolean): String {
        val sb = StringBuilder()
        for (line in lines) {
            line.getItems()?.forEach { item ->
                if (item.getText() == "\n") {
                    sb.append("<br/>")
                }
                if (item.isSelected()) {
                    sb.append("{{c1::${item.getText()}}}")
                } else {
                    sb.append(item.getText())
                }
            }
        }
        var result = sb.toString().replace("}}{{c1::", "").trim() // combine adjacent cloze
        if (result.length < 4) {
            return result
        }
        return if (multiCardMode) {
            val sbnew = StringBuilder(result)
            var closeCount = 1
            for (i in 3 until result.length) {
                val sub = result.substring(i - 3, i)
                if (sub == "{{c") {
                    sbnew.replace(i, i + 1, closeCount.toString())
                    closeCount++
                }
            }
            sbnew.toString()
        } else {
            result
        }
    }
}
