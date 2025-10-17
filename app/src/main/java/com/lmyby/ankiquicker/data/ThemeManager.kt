package com.lmyby.ankiquicker.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.lmyby.ankiquicker.R

/**
 * Theme management utility class for applying themes across the app with dark mode support
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object ThemeManager {

    /**
     * Apply theme to an activity with dark mode support
     */
    @JvmStatic
    fun applyTheme(activity: Activity) {
        val theme = Settings.getInstance(activity).getSelectedTheme()
        var themeResId = theme.themeResId

        // Apply dark mode override for themes that support system dark mode
        if (theme.supportsSystemDarkMode() && theme.shouldApplyDarkMode(activity)) {
            themeResId = R.style.Theme_AnkiQuickerDark
        }

        activity.setTheme(themeResId)
    }

    /**
     * Apply transparent theme to popup activities with dark mode support
     */
    @JvmStatic
    fun applyTransparentTheme(activity: Activity) {
        val theme = Settings.getInstance(activity).getSelectedTheme()
        var themeResId = theme.transparentThemeResId

        // Apply dark mode override for themes that support system dark mode
        if (theme.supportsSystemDarkMode() && theme.shouldApplyDarkMode(activity)) {
            themeResId = R.style.TransparentDark
        }

        activity.setTheme(themeResId)
    }

    /**
     * Check if current theme is e-ink optimized
     */
    @JvmStatic
    fun isEinkTheme(context: Context): Boolean {
        return Settings.getInstance(context).getSelectedTheme() == AppTheme.EINK
    }

    /**
     * Check if current theme is dark mode
     */
    @JvmStatic
    fun isDarkMode(context: Context): Boolean {
        val theme = Settings.getInstance(context).getSelectedTheme()
        return theme.isDarkMode() ||
                (theme.supportsSystemDarkMode() && theme.shouldApplyDarkMode(context))
    }

    /**
     * Get theme-appropriate color resource with dark mode support
     */
    @JvmStatic
    @JvmOverloads
    fun getThemeColor(
        context: Context,
        defaultColorRes: Int,
        pinkColorRes: Int,
        einkColorRes: Int,
        darkColorRes: Int = defaultColorRes
    ): Int {
        val theme = Settings.getInstance(context).getSelectedTheme()

        if (theme.supportsSystemDarkMode() && theme.shouldApplyDarkMode(context)) {
            return ContextCompat.getColor(context, darkColorRes)
        }

        return when (theme) {
            AppTheme.PINK -> ContextCompat.getColor(context, pinkColorRes)
            AppTheme.EINK -> ContextCompat.getColor(context, einkColorRes)
            AppTheme.DARK -> ContextCompat.getColor(context, darkColorRes)
            else -> ContextCompat.getColor(context, defaultColorRes)
        }
    }

    /**
     * Get theme-appropriate drawable resource with dark mode support
     */
    @JvmStatic
    @JvmOverloads
    fun getThemeDrawable(
        context: Context,
        defaultDrawableRes: Int,
        pinkDrawableRes: Int,
        einkDrawableRes: Int,
        darkDrawableRes: Int = defaultDrawableRes
    ): Int {
        val theme = Settings.getInstance(context).getSelectedTheme()

        if (theme.supportsSystemDarkMode() && theme.shouldApplyDarkMode(context)) {
            return darkDrawableRes
        }

        return when (theme) {
            AppTheme.PINK -> pinkDrawableRes
            AppTheme.EINK -> einkDrawableRes
            AppTheme.DARK -> darkDrawableRes
            else -> defaultDrawableRes
        }
    }

    /**
     * Restart app to apply theme changes
     */
    @JvmStatic
    fun restartAppForThemeChange(activity: Activity) {
        val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
            activity.finish()
            Runtime.getRuntime().exit(0)
        }
    }
}
