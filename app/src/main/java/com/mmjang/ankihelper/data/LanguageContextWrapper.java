package com.mmjang.ankihelper.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;
import java.util.Locale;

/**
 * Context wrapper for applying language changes without restarting the app
 * This follows modern Android best practices for dynamic language switching
 */
public class LanguageContextWrapper extends ContextWrapper {
    
    public LanguageContextWrapper(Context base) {
        super(base);
    }
    
    /**
     * Create a new context with the specified language
     * @param context The base context
     * @param language The target language
     * @return Context with updated language configuration
     */
    public static Context wrap(Context context, AppLanguage language) {
        Locale targetLocale = language.getEffectiveLocale(context);
        
        if (language == AppLanguage.SYSTEM) {
            targetLocale = Locale.getDefault();
        }
        
        Configuration config = new Configuration(context.getResources().getConfiguration());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(targetLocale);
            return context.createConfigurationContext(config);
        } else {
            // For older versions, update the configuration directly
            config.locale = targetLocale;
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            return context;
        }
    }
    
    /**
     * Update the base context language configuration
     * @param context The context to update
     * @param language The target language
     */
    public static void updateBaseContextLocale(Context context, AppLanguage language) {
        Locale targetLocale = language.getEffectiveLocale(context);
        
        if (language == AppLanguage.SYSTEM) {
            targetLocale = Locale.getDefault();
        }
        
        Configuration config = new Configuration();
        config.setLocale(targetLocale);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config);
        } else {
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }
    }
}