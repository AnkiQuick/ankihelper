package com.mmjang.ankihelper.data;

import android.content.Context;
import com.mmjang.ankihelper.R;

/**
 * Application theme enumeration
 * Defines available themes and their properties
 */
public enum AppTheme {
    DEFAULT("default", R.string.theme_name_default, R.style.Theme_AnkiHelper, R.style.Transparent),
    PINK("pink", R.string.theme_name_pink, R.style.Theme_AnkiHelperPink, R.style.TransparentPink),
    EINK("eink", R.string.theme_name_eink, R.style.Theme_AnkiHelperEink, R.style.Transparent);
    
    private final String key;
    private final int displayNameResId;
    private final int themeResId;
    private final int transparentThemeResId;
    
    AppTheme(String key, int displayNameResId, int themeResId, int transparentThemeResId) {
        this.key = key;
        this.displayNameResId = displayNameResId;
        this.themeResId = themeResId;
        this.transparentThemeResId = transparentThemeResId;
    }
    
    public String getKey() { return key; }
    public int getDisplayNameResId() { return displayNameResId; }
    public int getThemeResId() { return themeResId; }
    public int getTransparentThemeResId() { return transparentThemeResId; }
    
    /**
     * Get localized display name
     */
    public String getDisplayName(Context context) {
        return context.getString(displayNameResId);
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
}