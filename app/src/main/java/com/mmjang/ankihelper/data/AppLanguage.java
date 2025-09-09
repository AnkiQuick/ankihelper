package com.mmjang.ankihelper.data;

import java.util.Locale;
import com.mmjang.ankihelper.R;

/**
 * Language enumeration for AnkiHelper application
 * Supports system default, English, and Chinese languages
 */
public enum AppLanguage {
    SYSTEM("system", Locale.getDefault()),
    ENGLISH("en", Locale.ENGLISH),
    CHINESE("zh", Locale.SIMPLIFIED_CHINESE);
    
    private final String key;
    private final Locale locale;
    
    AppLanguage(String key, Locale locale) {
        this.key = key;
        this.locale = locale;
    }
    
    public String getKey() {
        return key;
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