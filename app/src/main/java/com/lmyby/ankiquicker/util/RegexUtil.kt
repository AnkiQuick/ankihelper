package com.lmyby.ankiquicker.util

import java.util.regex.Pattern

/**
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object RegexUtil {

    @JvmField
    val SYMBOL_REX_WITH_BLANK = """[ ,\./:"\\\[\]\|`~!@#\$%\^&\*\(\)_\+=<\->\?;'，。、；：''""【】《》？\{\}！￥…（）—=]"""

    @JvmField
    val SYMBOL_REX_WITHOUT_BLANK = """[,\./:"\\\[\]\|`~!! @#\$%\^&\*\(\)_\+=<\->\?;'，。、；：''""【】《》？\{\}！￥…（）—=]"""

    @JvmField
    var SYMBOL_REX = SYMBOL_REX_WITHOUT_BLANK

    //    @JvmStatic
    //    fun refreshSymbolSelection() {
    //        val b = SPHelper.getBoolean(ConstantUtil.TREAT_BLANKS_AS_SYMBOL, true)
    //        SYMBOL_REX = if (b) {
    //            SYMBOL_REX_WITH_BLANK
    //        } else {
    //            SYMBOL_REX_WITHOUT_BLANK
    //        }
    //    }

    @JvmStatic
    fun isEnglish(charaString: String): Boolean {
        return charaString.matches(Regex("^[a-zA-Z]*-*[a-zA-Z]*"))
    }

    @JvmStatic
    fun isSpecialWord(charaString: String): Boolean {
        return charaString.matches(Regex("^[a-zA-ZÀ-ÿ]*-*[a-zA-ZÀ-ÿ]*"))
    }

    @JvmStatic
    fun isKorean(ch: Char): Boolean {
        return ch in '가'..'힣'
    }

    @JvmStatic
    fun isPositiveInteger(orginal: String): Boolean {
        return isMatch("^\\+{0,1}[1-9]\\d*", orginal)
    }

    @JvmStatic
    fun isNegativeInteger(orginal: String): Boolean {
        return isMatch("^-[1-9]\\d*", orginal)
    }

    @JvmStatic
    fun isWholeNumber(orginal: String): Boolean {
        return isMatch("[+-]{0,1}0", orginal) || isPositiveInteger(orginal) || isNegativeInteger(orginal)
    }

    @JvmStatic
    fun isPositiveDecimal(orginal: String): Boolean {
        return isMatch("\\+{0,1}[0]\\.[1-9]*|\\+{0,1}[1-9]\\d*\\.\\d*", orginal)
    }

    @JvmStatic
    fun isNegativeDecimal(orginal: String): Boolean {
        return isMatch("^-[0]\\.[1-9]*|^-[1-9]\\d*\\.\\d*", orginal)
    }

    @JvmStatic
    fun isDecimal(orginal: String): Boolean {
        return isMatch("[-+]{0,1}\\d+\\.\\d*|[-+]{0,1}\\d*\\.\\d+", orginal)
    }

    @JvmStatic
    fun isNumber(orginal: String): Boolean {
        return isWholeNumber(orginal) || isDecimal(orginal) || isNegativeDecimal(orginal) || isPositiveDecimal(orginal)
    }

    private fun isMatch(regex: String, orginal: String?): Boolean {
        if (orginal.isNullOrBlank()) {
            return false
        }
        val pattern = Pattern.compile(regex)
        val isNum = pattern.matcher(orginal)
        return isNum.matches()
    }

    //    // 根据Unicode编码完美的判断中文汉字和符号
    //    @JvmStatic
    //    fun isChinese(c: Char): Boolean {
    //        val ub = Character.UnicodeBlock.of(c)
    //        return (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
    //                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
    //                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
    //                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
    //                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
    //                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
    //                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION)
    //    }

    /**
     * 输入的字符是否是汉字
     * @param a char
     * @return boolean
     */
    @JvmStatic
    fun isChinese(a: Char): Boolean {
        val v = a.code
        return v in 19968..171941
    }

    @JvmStatic
    fun isChineseSentence(s: String): Boolean {
        if (s.isEmpty()) return false
        var count = 0
        for (i in s.indices) {
            if (isChinese(s[i])) {
                count++
            }
        }
        return (count.toDouble() / s.length) >= 0.5
    }

    @JvmStatic
    fun isSymbol(a: Char): Boolean {
        val s = a.toString()
        return s.matches(Regex(SYMBOL_REX))
    }

    @JvmStatic
    fun isSymbol(a: String): Boolean {
        return a.matches(Regex(SYMBOL_REX))
    }
}
