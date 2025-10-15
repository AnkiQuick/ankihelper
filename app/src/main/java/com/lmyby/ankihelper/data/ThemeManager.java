package com.lmyby.ankihelper.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import com.lmyby.ankihelper.R;

/**
 * Theme management utility class for applying themes across the app with dark mode support
 */
public class ThemeManager {
    
    /**
     * Apply theme to an activity with dark mode support
     */
    public static void applyTheme(Activity activity) {
        AppTheme theme = Settings.getInstance(activity).getSelectedTheme();
        int themeResId = theme.getThemeResId();
        
        // Apply dark mode override for themes that support system dark mode
        if (theme.supportsSystemDarkMode() && theme.shouldApplyDarkMode(activity)) {
            themeResId = R.style.Theme_AnkiHelperDark;
        }
        
        activity.setTheme(themeResId);
    }
    
    /**
     * Apply transparent theme to popup activities with dark mode support
     */
    public static void applyTransparentTheme(Activity activity) {
        AppTheme theme = Settings.getInstance(activity).getSelectedTheme();
        int themeResId = theme.getTransparentThemeResId();
        
        // Apply dark mode override for themes that support system dark mode
        if (theme.supportsSystemDarkMode() && theme.shouldApplyDarkMode(activity)) {
            themeResId = R.style.TransparentDark;
        }
        
        activity.setTheme(themeResId);
    }
    
    /**
     * Check if current theme is e-ink optimized
     */
    public static boolean isEinkTheme(Context context) {
        return Settings.getInstance(context).getSelectedTheme() == AppTheme.EINK;
    }
    
    /**
     * Check if current theme is dark mode
     */
    public static boolean isDarkMode(Context context) {
        AppTheme theme = Settings.getInstance(context).getSelectedTheme();
        return theme.isDarkMode() || 
               (theme.supportsSystemDarkMode() && theme.shouldApplyDarkMode(context));
    }
    
    /**
     * Get theme-appropriate color resource with dark mode support
     */
    public static int getThemeColor(Context context, int defaultColorRes, 
                                   int pinkColorRes, int einkColorRes, int darkColorRes) {
        AppTheme theme = Settings.getInstance(context).getSelectedTheme();
        
        if (theme.supportsSystemDarkMode() && theme.shouldApplyDarkMode(context)) {
            return ContextCompat.getColor(context, darkColorRes);
        }
        
        switch (theme) {
            case PINK:
                return ContextCompat.getColor(context, pinkColorRes);
            case EINK:
                return ContextCompat.getColor(context, einkColorRes);
            case DARK:
                return ContextCompat.getColor(context, darkColorRes);
            default:
                return ContextCompat.getColor(context, defaultColorRes);
        }
    }
    
    /**
     * Get theme-appropriate color resource (legacy method for backward compatibility)
     */
    public static int getThemeColor(Context context, int defaultColorRes, 
                                   int pinkColorRes, int einkColorRes) {
        return getThemeColor(context, defaultColorRes, pinkColorRes, einkColorRes, defaultColorRes);
    }
    
    /**
     * Get theme-appropriate drawable resource with dark mode support
     */
    public static int getThemeDrawable(Context context, int defaultDrawableRes,
                                      int pinkDrawableRes, int einkDrawableRes, int darkDrawableRes) {
        AppTheme theme = Settings.getInstance(context).getSelectedTheme();
        
        if (theme.supportsSystemDarkMode() && theme.shouldApplyDarkMode(context)) {
            return darkDrawableRes;
        }
        
        switch (theme) {
            case PINK:
                return pinkDrawableRes;
            case EINK:
                return einkDrawableRes;
            case DARK:
                return darkDrawableRes;
            default:
                return defaultDrawableRes;
        }
    }
    
    /**
     * Get theme-appropriate drawable resource (legacy method for backward compatibility)
     */
    public static int getThemeDrawable(Context context, int defaultDrawableRes,
                                      int pinkDrawableRes, int einkDrawableRes) {
        return getThemeDrawable(context, defaultDrawableRes, pinkDrawableRes, einkDrawableRes, defaultDrawableRes);
    }
    
    /**
     * Restart app to apply theme changes
     */
    public static void restartAppForThemeChange(Activity activity) {
        Intent intent = activity.getPackageManager()
            .getLaunchIntentForPackage(activity.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            activity.finish();
            Runtime.getRuntime().exit(0);
        }
    }
}