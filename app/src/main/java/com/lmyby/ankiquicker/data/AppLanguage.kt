package com.lmyby.ankiquicker.data

import android.content.Context
import com.lmyby.ankiquicker.R
import java.util.Locale

/**
 * Language enumeration for AnkiHelper application
 * Supports system default, English, and Chinese languages
 * Uses modern AndroidX AppCompatDelegate API for language switching
 * Converted to Kotlin as part of Phase 2 data model migration
 */
enum class AppLanguage(
    val key: String,
    val languageTag: String, // IETF BCP 47 language tag for AndroidX
    val locale: Locale
) {
    SYSTEM("system", "", Locale.getDefault()),
    ENGLISH("en", "en", Locale.ENGLISH),
    CHINESE("zh", "zh", Locale.SIMPLIFIED_CHINESE);

    /**
     * Get localized display name based on context
     * @param context Android context for getting string resources
     * @return Localized display name
     */
    fun getDisplayName(context: Context): String {
        return when (this) {
            SYSTEM -> context.getString(R.string.language_system)
            ENGLISH -> context.getString(R.string.language_english)
            CHINESE -> context.getString(R.string.language_chinese)
        }
    }

    /**
     * Check if this language is system default
     * @return true if this is the system language option
     */
    fun isSystem(): Boolean {
        return this == SYSTEM
    }

    /**
     * Get the effective locale for this language
     * @param context Android context for getting system default if needed
     * @return The locale to use
     */
    fun getEffectiveLocale(context: Context): Locale {
        return if (this == SYSTEM) {
            Locale.getDefault()
        } else {
            locale
        }
    }

    companion object {
        /**
         * Get AppLanguage from key string
         * @param key The language key
         * @return AppLanguage enum value, defaults to SYSTEM if not found
         */
        @JvmStatic
        fun fromKey(key: String): AppLanguage {
            return values().find { it.key == key } ?: SYSTEM
        }
    }
}
