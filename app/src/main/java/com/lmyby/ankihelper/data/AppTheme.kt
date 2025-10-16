package com.lmyby.ankihelper.data

import android.content.Context
import com.lmyby.ankihelper.R

/**
 * Application theme enumeration
 * Defines available themes and their properties
 * Converted to Kotlin as part of Phase 2 data model migration
 */
enum class AppTheme(
    val key: String,
    val themeResId: Int,
    val transparentThemeResId: Int
) {
    DEFAULT("default", R.style.Theme_AnkiHelper, R.style.Transparent),
    PINK("pink", R.style.Theme_AnkiHelperPink, R.style.TransparentPink),
    EINK("eink", R.style.Theme_AnkiHelperEink, R.style.Transparent),
    DARK("dark", R.style.Theme_AnkiHelperDark, R.style.TransparentDark);

    /**
     * Get localized display name for this theme
     * @param context Context for accessing string resources
     * @return Localized theme name
     */
    fun getDisplayName(context: Context): String {
        return when (this) {
            DEFAULT -> context.getString(R.string.theme_default)
            PINK -> context.getString(R.string.theme_pink)
            EINK -> context.getString(R.string.theme_eink)
            DARK -> context.getString(R.string.theme_dark)
        }
    }

    /**
     * Check if this theme is specifically a dark mode theme
     */
    fun isDarkMode(): Boolean {
        return this == DARK
    }

    /**
     * Check if this theme supports automatic system dark mode
     */
    fun supportsSystemDarkMode(): Boolean {
        return this == DEFAULT || this == PINK
    }

    /**
     * Check if dark mode should be applied for this theme
     */
    fun shouldApplyDarkMode(context: Context): Boolean {
        return when (this) {
            DARK -> true
            EINK -> false // E-ink theme is always light for readability
            else -> isSystemDarkModeEnabled(context) // For DEFAULT and PINK themes, check system dark mode setting
        }
    }

    companion object {
        /**
         * Find theme by key, returns DEFAULT if not found
         */
        @JvmStatic
        fun fromKey(key: String?): AppTheme {
            if (key == null) return DEFAULT
            return values().find { it.key == key } ?: DEFAULT
        }

        /**
         * Find theme by ordinal, returns DEFAULT if out of bounds
         */
        @JvmStatic
        fun fromOrdinal(ordinal: Int): AppTheme {
            return if (ordinal < 0 || ordinal >= values().size) DEFAULT else values()[ordinal]
        }

        /**
         * Check if system dark mode is enabled
         */
        private fun isSystemDarkModeEnabled(context: Context): Boolean {
            val nightModeFlags = context.resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    }
}
