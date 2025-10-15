package com.lmyby.ankihelper.data;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

/**
 * Modern language management utility for AnkiHelper application
 * Uses AndroidX AppCompatDelegate API (API 33+ with backward compatibility)
 *
 * This implementation follows Android's official best practices for per-app language preferences.
 * The language setting is automatically persisted and applied system-wide for the app.
 */
public class LanguageManager {

    /**
     * Apply the selected language to the entire application
     * This is the main method to call when changing language
     *
     * @param language The language to apply
     */
    public static void setAppLanguage(AppLanguage language) {
        LocaleListCompat localeList;

        if (language == AppLanguage.SYSTEM || language.getLanguageTag().isEmpty()) {
            // Empty locale list means follow system default
            localeList = LocaleListCompat.getEmptyLocaleList();
        } else {
            // Set specific language
            localeList = LocaleListCompat.forLanguageTags(language.getLanguageTag());
        }

        // This will automatically:
        // 1. Persist the language preference
        // 2. Recreate all activities with new language
        // 3. Update configuration across the app
        // 4. Set Locale.setDefault() appropriately
        AppCompatDelegate.setApplicationLocales(localeList);
    }

    /**
     * Get the currently applied app language
     *
     * @return AppLanguage representing current language, or SYSTEM if following system default
     */
    public static AppLanguage getCurrentAppLanguage() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();

        if (locales.isEmpty()) {
            return AppLanguage.SYSTEM;
        }

        Locale currentLocale = locales.get(0);
        if (currentLocale != null) {
            String languageCode = currentLocale.getLanguage();

            // Match to our supported languages
            for (AppLanguage language : AppLanguage.values()) {
                if (language.getLocale().getLanguage().equals(languageCode)) {
                    return language;
                }
            }
        }

        return AppLanguage.SYSTEM;
    }

    /**
     * Get current language code
     *
     * @return Current language code (en, zh, etc.) or system default language code
     */
    public static String getCurrentLanguageCode() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();

        if (locales.isEmpty()) {
            return Locale.getDefault().getLanguage();
        }

        Locale currentLocale = locales.get(0);
        return currentLocale != null ? currentLocale.getLanguage() : Locale.getDefault().getLanguage();
    }

    /**
     * Check if current language is Chinese
     *
     * @return true if current language is Chinese
     */
    public static boolean isChinese() {
        return "zh".equals(getCurrentLanguageCode());
    }

    /**
     * Check if current language is English
     *
     * @return true if current language is English
     */
    public static boolean isEnglish() {
        return "en".equals(getCurrentLanguageCode());
    }

    /**
     * Sync Settings with AndroidX persisted language preference
     * Call this on app startup to ensure Settings reflects the actual app language
     *
     * @param context Android context
     */
    public static void syncWithSettings(Context context) {
        AppLanguage currentLanguage = getCurrentAppLanguage();
        Settings settings = Settings.getInstance(context);

        // Update settings to match AndroidX persisted language
        AppLanguage savedLanguage = settings.getSelectedLanguage();
        if (savedLanguage != currentLanguage) {
            settings.setSelectedLanguage(currentLanguage);
        }
    }
}
