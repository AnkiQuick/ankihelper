package com.lmyby.ankiquicker.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Modern language management utility for AnkiHelper application
 * Uses AndroidX AppCompatDelegate API (API 33+ with backward compatibility)
 *
 * This implementation follows Android's official best practices for per-app language preferences.
 * The language setting is automatically persisted and applied system-wide for the app.
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object LanguageManager {

    /**
     * Apply the selected language to the entire application
     * This is the main method to call when changing language
     *
     * @param language The language to apply
     */
    @JvmStatic
    fun setAppLanguage(language: AppLanguage) {
        val localeList = if (language == AppLanguage.SYSTEM || language.languageTag.isEmpty()) {
            // Empty locale list means follow system default
            LocaleListCompat.getEmptyLocaleList()
        } else {
            // Set specific language
            LocaleListCompat.forLanguageTags(language.languageTag)
        }

        // This will automatically:
        // 1. Persist the language preference
        // 2. Recreate all activities with new language
        // 3. Update configuration across the app
        // 4. Set Locale.setDefault() appropriately
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * Get the currently applied app language
     *
     * @return AppLanguage representing current language, or SYSTEM if following system default
     */
    @JvmStatic
    fun getCurrentAppLanguage(): AppLanguage {
        val locales = AppCompatDelegate.getApplicationLocales()

        if (locales.isEmpty) {
            return AppLanguage.SYSTEM
        }

        val currentLocale = locales[0]
        if (currentLocale != null) {
            val languageCode = currentLocale.language

            // Match to our supported languages
            for (language in AppLanguage.values()) {
                if (language.locale.language == languageCode) {
                    return language
                }
            }
        }

        return AppLanguage.SYSTEM
    }

    /**
     * Get current language code
     *
     * @return Current language code (en, zh, etc.) or system default language code
     */
    @JvmStatic
    fun getCurrentLanguageCode(): String {
        val locales = AppCompatDelegate.getApplicationLocales()

        if (locales.isEmpty) {
            return Locale.getDefault().language
        }

        val currentLocale = locales[0]
        return currentLocale?.language ?: Locale.getDefault().language
    }

    /**
     * Check if current language is Chinese
     *
     * @return true if current language is Chinese
     */
    @JvmStatic
    fun isChinese(): Boolean {
        return "zh" == getCurrentLanguageCode()
    }

    /**
     * Check if current language is English
     *
     * @return true if current language is English
     */
    @JvmStatic
    fun isEnglish(): Boolean {
        return "en" == getCurrentLanguageCode()
    }

    /**
     * Sync Settings with AndroidX persisted language preference
     * Call this on app startup to ensure Settings reflects the actual app language
     *
     * @param context Android context
     */
    @JvmStatic
    fun syncWithSettings(context: Context) {
        val currentLanguage = getCurrentAppLanguage()
        val settings = Settings.getInstance(context)

        // Update settings to match AndroidX persisted language
        val savedLanguage = settings.getSelectedLanguage()
        if (savedLanguage != currentLanguage) {
            settings.setSelectedLanguage(currentLanguage)
        }
    }
}
