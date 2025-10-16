package com.lmyby.ankihelper.util.com.baidu.translate.demo

import kotlin.random.Random

/**
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object RandomAPIKeyGenerator {

    private val APP_ID_AND_KEY_LIST = arrayOf(
//            "20181126000239193\nCPhOj0FHGhvt2f5x5kag",
//            "20180208000121840\n6vcjzWbWq5Swqk8y_VQG",
//            "20181125000239165\nXVCzhWeP3QLLzW7TXHGm",
//            "20181125000239170\np4CI4cEngtYvRx12HUec",
        "20160220000012831\nISSPx0K_ZyrUN9IAOKel"
    )

    @JvmStatic
    fun next(): Array<String> {
        val min = 0
        val max = APP_ID_AND_KEY_LIST.size - 1
        val index = randInt(min, max)
        return APP_ID_AND_KEY_LIST[index].split("\n").toTypedArray()
    }

    @JvmStatic
    fun randInt(min: Int, max: Int): Int {
        return Random.nextInt(min, max + 1)
    }
}
