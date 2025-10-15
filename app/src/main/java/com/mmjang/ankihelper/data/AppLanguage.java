package com.mmjang.ankihelper.data;

import java.util.Locale;
import com.mmjang.ankihelper.R;

/**
 * Language enumeration for AnkiHelper application
 * Supports system default, English, and Chinese languages
 * Uses modern AndroidX AppCompatDelegate API for language switching
 */
public enum AppLanguage {
    SYSTEM("system", "", Locale.getDefault()),
    ENGLISH("en", "en", Locale.ENGLISH),
    CHINESE("zh", "zh", Locale.SIMPLIFIED_CHINESE);

    private final String key;
    private final String languageTag; // IETF BCP 47 language tag for AndroidX
    private final Locale locale;

    AppLanguage(String key, String languageTag, Locale locale) {
        this.key = key;
        this.languageTag = languageTag;
        this.locale = locale;
    }

    public String getKey() {
        return key;
    }

    /**
     * Get the IETF BCP 47 language tag for AndroidX AppCompatDelegate
     * @return Language tag string (empty string for system default)
     */
    public String getLanguageTag() {
        return languageTag;
    }
    
    /**
     * Get localized display name based on context
     * @param context Android context for getting string resources
     * @return Localized display name
     */
    public String getDisplayName(android.content.Context context) {
        switch (this) {
            case SYSTEM:
                return context.getString(R.string.language_system);
            case ENGLISH:
                return context.getString(R.string.language_english);
            case CHINESE:
                return context.getString(R.string.language_chinese);
            default:
                return name();
        }
    }
    
    public Locale getLocale() {
        return locale;
    }
    
    /**
     * Get AppLanguage from key string
     * @param key The language key
     * @return AppLanguage enum value, defaults to SYSTEM if not found
     */
    public static AppLanguage fromKey(String key) {
        for (AppLanguage language : values()) {
            if (language.key.equals(key)) {
                return language;
            }
        }
        return SYSTEM;
    }
    
    /**
     * Check if this language is system default
     * @return true if this is the system language option
     */
    public boolean isSystem() {
        return this == SYSTEM;
    }
    
    /**
     * Get the effective locale for this language
     * @param context Android context for getting system default if needed
     * @return The locale to use
     */
    public Locale getEffectiveLocale(android.content.Context context) {
        if (this == SYSTEM) {
            return Locale.getDefault();
        }
        return locale;
    }
}