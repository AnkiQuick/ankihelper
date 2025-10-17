package com.lmyby.ankiquicker.domain

import android.content.Context
import com.lmyby.ankiquicker.R

/**
 * Manager for pronunciation languages and Youdao API types
 * Converted to Kotlin as part of Phase 3 domain logic migration
 */
object PronounceManager {

    // Deprecated: Kept for backward compatibility only
    // Use getAvailablePronounceLanguage(Context) instead
    @Deprecated("Use getAvailablePronounceLanguage(Context) instead")
    const val LANGUAGE_ENGLISH = "英语发音"
    @Deprecated("Use getAvailablePronounceLanguage(Context) instead")
    const val LANGUAGE_FRENCH = "法语发音"
    @Deprecated("Use getAvailablePronounceLanguage(Context) instead")
    const val LANGUAGE_JAPANESE = "日语发音"
    @Deprecated("Use getAvailablePronounceLanguage(Context) instead")
    const val LANGUAGE_KOREAN = "韩语发音"
    @Deprecated("Use getAvailablePronounceLanguage(Context) instead")
    const val LANGUAGE_GERMAN = "德语发音"

    // Deprecated aliases for backward compatibility (typo/naming fixes)
    @Deprecated("Use LANGUAGE_FRENCH instead")
    const val LANGUAGE_FRENCHE = LANGUAGE_FRENCH
    @Deprecated("Use LANGUAGE_GERMAN instead")
    const val LANGUAGE_GERMANY = LANGUAGE_GERMAN

    const val LANGUAGE_ENGLISH_INDEX = 0
    const val LANGUAGE_FRENCH_INDEX = 1
    const val LANGUAGE_JAPANESE_INDEX = 2
    const val LANGUAGE_KOREAN_INDEX = 3
    const val LANGUAGE_GERMANY_INDEX = 4

    const val YOUDAO_PRONOUNCE_TYPE_ENGLISH = "eng"
    const val YOUDAO_PRONOUNCE_TYPE_FRENCH = "fr"
    const val YOUDAO_PRONOUNCE_TYPE_JAPANESE = "jap"
    const val YOUDAO_PRONOUNCE_TYPE_KOREAN = "ko"
    const val YOUDAO_PRONOUNCE_TYPE_GERMANY = "ger"

    /**
     * Get available pronunciation languages with localized names
     * @param context Context to load string resources
     * @return Array of localized pronunciation language names
     */
    @JvmStatic
    fun getAvailablePronounceLanguage(context: Context): Array<String> {
        return arrayOf(
            context.getString(R.string.pronounce_language_english),
            context.getString(R.string.pronounce_language_french),
            context.getString(R.string.pronounce_language_japanese),
            context.getString(R.string.pronounce_language_korean),
            context.getString(R.string.pronounce_language_german)
        )
    }

    /**
     * @deprecated Use {@link #getAvailablePronounceLanguage(Context)} instead
     */
    @Deprecated("Use getAvailablePronounceLanguage(Context) instead")
    @JvmStatic
    fun getAvailablePronounceLanguage(): Array<String> {
        return arrayOf(
            LANGUAGE_ENGLISH,
            LANGUAGE_FRENCH,
            LANGUAGE_JAPANESE,
            LANGUAGE_KOREAN,
            LANGUAGE_GERMAN
        )
    }

    @JvmStatic
    fun getYoudaoTypeFromLanguageIndex(index: Int): String {
        return when (index) {
            LANGUAGE_ENGLISH_INDEX -> YOUDAO_PRONOUNCE_TYPE_ENGLISH
            LANGUAGE_FRENCH_INDEX -> YOUDAO_PRONOUNCE_TYPE_FRENCH
            LANGUAGE_JAPANESE_INDEX -> YOUDAO_PRONOUNCE_TYPE_JAPANESE
            LANGUAGE_KOREAN_INDEX -> YOUDAO_PRONOUNCE_TYPE_KOREAN
            LANGUAGE_GERMANY_INDEX -> YOUDAO_PRONOUNCE_TYPE_GERMANY
            else -> YOUDAO_PRONOUNCE_TYPE_ENGLISH
        }
    }
}
