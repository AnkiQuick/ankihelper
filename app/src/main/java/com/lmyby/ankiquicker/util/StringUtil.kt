package com.lmyby.ankiquicker.util

/**
 * Created by chenxiangjie on 2017/7/26.
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object StringUtil {
    /**
     * 判断字符串是否为null或全为空白字符
     *
     * @param s 待校验字符串
     * @return `true`: null或全空白字符<br></br> `false`: 不为null且不全空白字符
     */
    @JvmStatic
    fun isSpace(s: String?): Boolean {
        if (s == null) return true
        for (element in s) {
            if (!Character.isWhitespace(element)) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun isSpace(c: Char): Boolean {
        return Character.isSpaceChar(c) || Character.isWhitespace(c)
    }
}
