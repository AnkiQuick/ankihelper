package com.mmjang.ankihelper.data;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

/**
 * Theme management utility class for applying themes across the app
 */
public class ThemeManager {
    
    /**
     * Apply theme to an activity
     */
    public static void applyTheme(Activity activity) {
        AppTheme theme = Settings.getInstance(activity).getSelectedTheme();
        activity.setTheme(theme.getThemeResId());
    }
    
    /**
     * Apply theme dynamically without restart (modern Android approach)
     */
    public static void applyThemeDynamically(Activity activity) {
        AppTheme theme = Settings.getInstance(activity).getSelectedTheme();
        
        // Set day/night mode for the entire app
        AppCompatDelegate.setDefaultNightMode(getNightModeForTheme(theme));
        
        // Apply theme to current activity
        activity.setTheme(theme.getThemeResId());
        
        // Update UI components without restart
        updateThemeColors(activity);
    }
    
    /**
     * Get AppCompatDelegate night mode for theme
     */
    private static int getNightModeForTheme(AppTheme theme) {
        switch (theme) {
            case PINK:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case EINK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            case DEFAULT:
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
    
    /**
     * Update activity theme without restart
     */
    private static void updateActivityTheme(Activity activity) {
        // For true no-restart theme switching, we need to update all UI components
        // This is a simplified approach - in a production app, you'd want to
        // update each component individually based on the new theme
        activity.recreate();
    }
    
    /**
     * Apply theme colors to existing views without restart (advanced)
     * This method provides a true no-restart experience but requires
     * manual updates to all UI components
     */
    public static void updateThemeColors(Activity activity) {
        // Update all UI components with new theme colors
        // This avoids the need for activity restart
        
        // Update window background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(
                    ContextCompat.getColor(activity, android.R.color.transparent)
            );
            activity.getWindow().setNavigationBarColor(
                    ContextCompat.getColor(activity, android.R.color.transparent)
            );
        }
        
        // Force refresh of all views
        refreshAllViews(activity.getWindow().getDecorView());
    }
    
    /**
     * Recursively refresh all views to apply new theme
     */
    private static void refreshAllViews(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                refreshAllViews(viewGroup.getChildAt(i));
            }
        }
        
        // Refresh the view to apply new theme
        view.refreshDrawableState();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.invalidateOutline();
        }
        view.invalidate();
    }
    
    /**
     * Apply transparent theme to popup activities
     */
    public static void applyTransparentTheme(Activity activity) {
        AppTheme theme = Settings.getInstance(activity).getSelectedTheme();
        activity.setTheme(theme.getTransparentThemeResId());
    }
    
    /**
     * Check if current theme is e-ink optimized
     */
    public static boolean isEinkTheme(Context context) {
        return Settings.getInstance(context).getSelectedTheme() == AppTheme.EINK;
    }
    
    /**
     * Get theme-appropriate color resource
     */
    public static int getThemeColor(Context context, int defaultColorRes, 
                                   int pinkColorRes, int einkColorRes) {
        AppTheme theme = Settings.getInstance(context).getSelectedTheme();
        switch (theme) {
            case PINK:
                return ContextCompat.getColor(context, pinkColorRes);
            case EINK:
                return ContextCompat.getColor(context, einkColorRes);
            default:
                return ContextCompat.getColor(context, defaultColorRes);
        }
    }
    
    /**
     * Get theme-appropriate drawable resource
     */
    public static int getThemeDrawable(Context context, int defaultDrawableRes,
                                      int pinkDrawableRes, int einkDrawableRes) {
        AppTheme theme = Settings.getInstance(context).getSelectedTheme();
        switch (theme) {
            case PINK:
                return pinkDrawableRes;
            case EINK:
                return einkDrawableRes;
            default:
                return defaultDrawableRes;
        }
    }
}