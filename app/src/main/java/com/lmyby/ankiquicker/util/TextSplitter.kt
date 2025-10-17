package com.lmyby.ankiquicker.util

import android.util.Log

/**
 * Created by liao on 2017/5/4.
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object TextSplitter {

    private const val DEVIDER = "__DEVIDER___DEVIDER__"
    private const val TAG = "TextSplitter"
    private const val MAX_TEXT_LENGTH = 20000 // Increased limit for better multi-line text support

    @JvmStatic
    fun getLocalSegments(str: String?): List<String> {
        Log.d(TAG, "getLocalSegments called with text length: ${str?.length ?: 0}")

        var processedStr = str ?: ""

        // Limit text length to prevent performance issues
        if (processedStr.length > MAX_TEXT_LENGTH) {
            Log.w(TAG, "Text is very long (${processedStr.length} chars), truncating to $MAX_TEXT_LENGTH chars")
            // Find the last complete sentence or word boundary to avoid cutting in middle of word
            processedStr = processedStr.substring(0, MAX_TEXT_LENGTH)
            val lastSentenceEnd = maxOf(
                processedStr.lastIndexOf('.'),
                processedStr.lastIndexOf('!'),
                processedStr.lastIndexOf('?')
            )
            val lastWordBoundary = processedStr.lastIndexOf(' ')
            val cutPoint = maxOf(lastSentenceEnd, lastWordBoundary)
            if (cutPoint > MAX_TEXT_LENGTH * 0.9) { // Only cut if we can preserve most of the text
                processedStr = processedStr.substring(0, cutPoint + 1)
            }
        }

        processedStr = preProcess(processedStr)
        val txts = mutableListOf<String>()
        val s = StringBuilder()

        for (i in processedStr.indices) {
            val first = processedStr[i]
            // 当到达末尾的时候
            if (i + 1 >= processedStr.length) {
                s.append(first)
                break
            }
            val next = processedStr[i + 1]

            when {
                (RegexUtil.isChinese(first) && !RegexUtil.isChinese(next)) ||
                (!RegexUtil.isChinese(first) && RegexUtil.isChinese(next)) ||
                (Character.isLetter(first) && !Character.isLetter(next)) ||
                (Character.isDigit(first) && !Character.isDigit(next)) ||
                (RegexUtil.isKorean(first) && !RegexUtil.isKorean(next)) ||
                (!RegexUtil.isKorean(first) && RegexUtil.isKorean(next)) -> {
                    s.append(first).append(DEVIDER)
                }

                RegexUtil.isSymbol(first) || StringUtil.isSpace(first) ||
                first == Constant.LEFT_BOLD_SUBSTITUDE[0] ||
                first == Constant.RIGHT_BOLD_SUBSTITUDE[0] -> {
                    s.append(DEVIDER).append(first).append(DEVIDER)
                }

                else -> {
                    s.append(first)
                }
            }
        }

        processedStr = s.toString()
        processedStr = processedStr.replace("\n", "$DEVIDER\n$DEVIDER")
        val texts = processedStr.split(DEVIDER)
        Log.d(TAG, "Split into ${texts.size} parts")

        for (text in texts) {
            if (text == DEVIDER) continue

            when {
                RegexUtil.isEnglish(text) -> {
                    txts.add(text)
                    continue
                }

                RegexUtil.isSpecialWord(text) -> {
                    txts.add(text)
                    continue
                }

                RegexUtil.isNumber(text) -> {
                    txts.add(text)
                    continue
                }

                else -> {
                    for (element in text) {
                        txts.add(element.toString())
                    }
                }
            }
        }

        Log.d(TAG, "getLocalSegments returning ${txts.size} segments")
        return txts
    }

    private fun preProcess(str: String?): String {
        Log.d(TAG, "preProcess called with text length: ${str?.length ?: 0}")
        if (str == null) return ""

        var processedStr = str.replace("<b>", Constant.LEFT_BOLD_SUBSTITUDE)
            .replace("</b>", Constant.RIGHT_BOLD_SUBSTITUDE)

        return if (!processedStr.contains("<br/>") && !processedStr.contains("<br>")) {
            processedStr
        } else {
            // html mode
            processedStr.replace("\n", "")
                .replace("<br/><br/>", "<br/>")
                .replace("<br><br>", "<br/>")
                .replace("<br/>", "\n")
                .replace("<br>", "\n")
        }
    }
}
