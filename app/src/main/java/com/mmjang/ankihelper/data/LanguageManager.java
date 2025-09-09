package com.mmjang.ankihelper.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.mmjang.ankihelper.MyApplication;

import java.util.Locale;

/**
 * Language management utility for AnkiHelper application
 * Handles language configuration, application, and switching
 */
public class LanguageManager {
    
    /**
     * Apply language configuration to the application context without restart
     * @param context Android context
     */
    public static void applyLanguage(Context context) {
        AppLanguage language = Settings.getInstance(context).getSelectedLanguage();
        applyLanguage(context, language);
    }
    
    /**
     * Apply language configuration to the application context without restart
     * @param context Android context
     * @param language Specific language to apply
     */
    public static void applyLanguage(Context context, AppLanguage language) {
        // Use modern context wrapper approach
        LanguageContextWrapper.updateBaseContextLocale(context, language);
        
        // Update resources configuration
        updateConfiguration(context, language.getEffectiveLocale(context));
    }
    
    /**
     * Update configuration with specified locale
     * @param context Android context
     * @param locale Target locale
     */
    private static void updateConfiguration(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        
        // Set locale
        config.setLocale(locale);
        
        // Update configuration
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        
        // Also update application context
        Context appContext = MyApplication.getContext();
        if (appContext != null && appContext != context) {
            Resources appResources = appContext.getResources();
            Configuration appConfig = appResources.getConfiguration();
            appConfig.setLocale(locale);
            appResources.updateConfiguration(appConfig, appResources.getDisplayMetrics());
        }
    }
    
    /**
     * Refresh activity to apply language changes without restart
     * @param activity Activity to refresh
     */
    public static void refreshActivityForLanguageChange(Activity activity) {
        // Apply language immediately
        applyLanguage(activity);
        
        // Refresh the activity by recreating it
        activity.recreate();
    }
    
    /**
     * Check if current language is RTL (Right-to-Left)
     * @param context Android context
     * @return true if current language is RTL
     */
    public static boolean isRTL(Context context) {
        Locale locale = Settings.getInstance(context).getEffectiveLocale();
        return isLocaleRTL(locale);
    }
    
    /**
     * Check if a specific locale is RTL
     * @param locale Locale to check
     * @return true if locale is RTL
     */
    private static boolean isLocaleRTL(Locale locale) {
        final byte directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
               directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }
    
    /**
     * Get current language code
     * @param context Android context
     * @return Current language code (en, zh, etc.)
     */
    public static String getCurrentLanguageCode(Context context) {
        AppLanguage language = Settings.getInstance(context).getSelectedLanguage();
        if (language == AppLanguage.SYSTEM) {
            return Locale.getDefault().getLanguage();
        }
        return language.getLocale().getLanguage();
    }
    
    /**
     * Get localized string resource
     * @param context Android context
     * @param stringId String resource ID
     * @return Localized string
     */
    public static String getString(Context context, int stringId) {
        return context.getString(stringId);
    }
    
    /**
     * Check if language is currently Chinese
     * @param context Android context
     * @return true if current language is Chinese
     */
    public static boolean isChinese(Context context) {
        String languageCode = getCurrentLanguageCode(context);
        return "zh".equals(languageCode);
    }
    
    /**
     * Check if language is currently English
     * @param context Android context
     * @return true if current language is English
     */
    public static boolean isEnglish(Context context) {
        String languageCode = getCurrentLanguageCode(context);
        return "en".equals(languageCode);
    }
    
    /**
     * Force update application-wide language configuration
     * This should be called when language is changed to ensure all components
     * are updated with the new language setting
     * @param context Android context
     */
    public static void forceUpdateApplicationLanguage(Context context) {
        AppLanguage language = Settings.getInstance(context).getSelectedLanguage();
        Locale targetLocale = language.getEffectiveLocale(context);
        
        // Update configuration for all resources
        updateConfiguration(context, targetLocale);
        
        // Force system services to update
        Configuration config = new Configuration();
        config.setTo(context.getResources().getConfiguration());
        config.setLocale(targetLocale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}