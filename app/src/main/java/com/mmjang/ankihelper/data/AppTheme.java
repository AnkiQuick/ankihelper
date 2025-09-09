package com.mmjang.ankihelper.data;

import android.content.Context;
import com.mmjang.ankihelper.R;

/**
 * Application theme enumeration
 * Defines available themes and their properties
 */
public enum AppTheme {
    DEFAULT("default", R.style.Theme_AnkiHelper, R.style.Transparent),
    PINK("pink", R.style.Theme_AnkiHelperPink, R.style.TransparentPink),
    EINK("eink", R.style.Theme_AnkiHelperEink, R.style.Transparent),
    DARK("dark", R.style.Theme_AnkiHelperDark, R.style.TransparentDark);
    
    private final String key;
    private final int themeResId;
    private final int transparentThemeResId;
    
    AppTheme(String key, int themeResId, int transparentThemeResId) {
        this.key = key;
        this.themeResId = themeResId;
        this.transparentThemeResId = transparentThemeResId;
    }
    
    public String getKey() { return key; }
    public int getThemeResId() { return themeResId; }
    public int getTransparentThemeResId() { return transparentThemeResId; }
    
    /**
     * Get localized display name for this theme
     * @param context Context for accessing string resources
     * @return Localized theme name
     */
    public String getDisplayName(Context context) {
        switch (this) {
            case DEFAULT:
                return context.getString(R.string.theme_default);
            case PINK:
                return context.getString(R.string.theme_pink);
            case EINK:
                return context.getString(R.string.theme_eink);
            case DARK:
                return context.getString(R.string.theme_dark);
            default:
                return name();
        }
    }
    
    /**
     * Find theme by key, returns DEFAULT if not found
     */
    public static AppTheme fromKey(String key) {
        if (key == null) return DEFAULT;
        
        for (AppTheme theme : values()) {
            if (theme.key.equals(key)) return theme;
        }
        return DEFAULT;
    }
    
    /**
     * Find theme by ordinal, returns DEFAULT if out of bounds
     */
    public static AppTheme fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) return DEFAULT;
        return values()[ordinal];
    }
    
    /**
     * Check if this theme is specifically a dark mode theme
     */
    public boolean isDarkMode() {
        return this == DARK;
    }
    
    /**
     * Check if this theme supports automatic system dark mode
     */
    public boolean supportsSystemDarkMode() {
        return this == DEFAULT || this == PINK;
    }
    
    /**
     * Check if dark mode should be applied for this theme
     */
    public boolean shouldApplyDarkMode(Context context) {
        if (this == DARK) {
            return true;
        }
        if (this == EINK) {
            return false; // E-ink theme is always light for readability
        }
        // For DEFAULT and PINK themes, check system dark mode setting
        return isSystemDarkModeEnabled(context);
    }
    
    /**
     * Check if system dark mode is enabled
     */
    private static boolean isSystemDarkModeEnabled(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode 
            & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }
}