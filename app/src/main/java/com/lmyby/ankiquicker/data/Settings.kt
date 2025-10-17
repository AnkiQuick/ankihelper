package com.lmyby.ankiquicker.data

import android.content.Context
import android.content.SharedPreferences
import com.lmyby.ankiquicker.domain.PronounceManager
import java.util.Locale

/**
 * Settings singleton for managing application preferences
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object Settings {
    private const val PREFER_NAME = "settings"    // 应用设置名称
    private const val MODEL_ID = "model_id"       // 应用设置项 模版id
    private const val DECK_ID = "deck_id"         // 应用设置项 牌组id
    private const val DEFAULT_MODEL_ID = "default_model_id" // 默认模版id，如果此选项存在，则已写入配套模版
    private const val FIELDS_MAP = "fields_map"   // 字段映射
    private const val MONITE_CLIPBOARD_Q = "show_clipboard_notification_q"   // 是否监听剪切板
    private const val AUTO_CANCEL_POPUP_Q = "auto_cancel_popup"              // 点加号后是否退出
    private const val LAST_SELECTED_PLAN = "last_selected_plan"
    private const val DEFAULT_TAG = "default_tag"
    private const val SET_AS_DEFAULT_TAG = "set_as_default_tag"
    private const val LAST_PRONOUNCE_LANGUAGE = "last_pronounce_language"
    private const val LEFT_HAND_MODE_Q = "left_hand_mode_q"
    private const val PINK_THEME_Q = "pink_theme_q"
    private const val SELECTED_THEME = "selected_theme"
    private const val THEME_MIGRATED = "theme_migrated"
    private const val OLD_DATA_MIGRATED = "old_data_migrated"

    // Language management settings
    private const val SELECTED_LANGUAGE = "selected_language"
    private const val SHOW_CONTENT_ALREADY_READ = "show_content_already_read"
    private const val FIRST_TIME_RUNNING_READER = "first_time_running_reader"

    // Baidu translator settings removed in Phase 15 - only AI translator is used now

    private lateinit var sp: SharedPreferences

    /**
     * Initialize Settings with application context
     * Must be called before using any other methods
     */
    @JvmStatic
    fun getInstance(context: Context): Settings {
        if (!::sp.isInitialized) {
            sp = context.getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE)
        }
        return this
    }

    /*************/

    internal fun getModelId(): Long {
        return sp.getLong(MODEL_ID, 0)
    }

    internal fun setModelId(modelId: Long) {
        sp.edit().putLong(MODEL_ID, modelId).apply()
    }

    /**************/

    internal fun getDeckId(): Long {
        return sp.getLong(DECK_ID, 0)
    }

    internal fun setDeckId(deckId: Long) {
        sp.edit().putLong(DECK_ID, deckId).apply()
    }

    /**************/

    internal fun getDefaultModelId(): Long {
        return sp.getLong(DEFAULT_MODEL_ID, 0)
    }

    internal fun setDefaultModelId(defaultModelId: Long) {
        sp.edit().putLong(DEFAULT_MODEL_ID, defaultModelId).apply()
    }

    /**************/

    internal fun getFieldsMap(): String {
        return sp.getString(FIELDS_MAP, "") ?: ""
    }

    internal fun setFieldsMap(fieldsMap: String) {
        sp.edit().putString(FIELDS_MAP, fieldsMap).apply()
    }

    /**************/

    @JvmStatic
    fun getMoniteClipboardQ(): Boolean {
        return sp.getBoolean(MONITE_CLIPBOARD_Q, false)
    }

    @JvmStatic
    fun setMoniteClipboardQ(moniteClipboardQ: Boolean) {
        sp.edit().putBoolean(MONITE_CLIPBOARD_Q, moniteClipboardQ).apply()
    }

    /**************/

    @JvmStatic
    fun getAutoCancelPopupQ(): Boolean {
        return sp.getBoolean(AUTO_CANCEL_POPUP_Q, false)
    }

    @JvmStatic
    fun setAutoCancelPopupQ(autoCancelPopupQ: Boolean) {
        sp.edit().putBoolean(AUTO_CANCEL_POPUP_Q, autoCancelPopupQ).apply()
    }

    @JvmStatic
    fun getLastSelectedPlan(): String {
        return sp.getString(LAST_SELECTED_PLAN, "") ?: ""
    }

    @JvmStatic
    fun setLastSelectedPlan(lastSelectedPlan: String) {
        sp.edit().putString(LAST_SELECTED_PLAN, lastSelectedPlan).apply()
    }

    /*****************/
    @JvmStatic
    fun getDefaulTag(): String {
        return sp.getString(DEFAULT_TAG, "") ?: ""
    }

    @JvmStatic
    fun setDefaultTag(defaultTag: String) {
        sp.edit().putString(DEFAULT_TAG, defaultTag).apply()
    }

    /****************/
    @JvmStatic
    fun getSetAsDefaultTag(): Boolean {
        return sp.getBoolean(SET_AS_DEFAULT_TAG, false)
    }

    @JvmStatic
    fun setSetAsDefaultTag(setAsDefaultTag: Boolean) {
        sp.edit().putBoolean(SET_AS_DEFAULT_TAG, setAsDefaultTag).apply()
    }

    @JvmStatic
    fun getLastPronounceLanguage(): Int {
        return sp.getInt(LAST_PRONOUNCE_LANGUAGE, PronounceManager.LANGUAGE_ENGLISH_INDEX)
    }

    @JvmStatic
    fun setLastPronounceLanguage(lastPronounceLanguageIndex: Int) {
        sp.edit().putInt(LAST_PRONOUNCE_LANGUAGE, lastPronounceLanguageIndex).apply()
    }

    @JvmStatic
    fun getLeftHandModeQ(): Boolean {
        return sp.getBoolean(LEFT_HAND_MODE_Q, false)
    }

    @JvmStatic
    fun setLeftHandModeQ(leftHandModeQ: Boolean) {
        sp.edit().putBoolean(LEFT_HAND_MODE_Q, leftHandModeQ).apply()
    }

    @JvmStatic
    fun getPinkThemeQ(): Boolean {
        // Use new theme system if available, fall back to old setting
        if (sp.getBoolean(THEME_MIGRATED, false)) {
            return getSelectedTheme() == AppTheme.PINK
        }
        return sp.getBoolean(PINK_THEME_Q, false)
    }

    @JvmStatic
    fun setPinkThemeQ(pinkThemeQ: Boolean) {
        sp.edit().putBoolean(PINK_THEME_Q, pinkThemeQ).apply()
    }

    /**
     * Get the currently selected theme
     */
    @JvmStatic
    fun getSelectedTheme(): AppTheme {
        // Handle migration from old pink theme setting
        if (!sp.getBoolean(THEME_MIGRATED, false)) {
            migrateThemeSettings()
        }

        val themeKey = sp.getString(SELECTED_THEME, AppTheme.DEFAULT.key) ?: AppTheme.DEFAULT.key
        return AppTheme.fromKey(themeKey)
    }

    /**
     * Set the selected theme
     */
    @JvmStatic
    fun setSelectedTheme(theme: AppTheme) {
        sp.edit().putString(SELECTED_THEME, theme.key).apply()
    }

    /**
     * Migrate from old pink theme boolean to new theme system
     */
    private fun migrateThemeSettings() {
        val editor = sp.edit()
        if (sp.getBoolean(PINK_THEME_Q, false)) {
            editor.putString(SELECTED_THEME, AppTheme.PINK.key)
        } else {
            editor.putString(SELECTED_THEME, AppTheme.DEFAULT.key)
        }
        editor.putBoolean(THEME_MIGRATED, true)
        editor.apply()
    }

    /**
     * Get the currently selected language
     */
    @JvmStatic
    fun getSelectedLanguage(): AppLanguage {
        val languageKey = sp.getString(SELECTED_LANGUAGE, AppLanguage.SYSTEM.key) ?: AppLanguage.SYSTEM.key
        return AppLanguage.fromKey(languageKey)
    }

    /**
     * Set the selected language
     */
    @JvmStatic
    fun setSelectedLanguage(language: AppLanguage) {
        sp.edit().putString(SELECTED_LANGUAGE, language.key).apply()
    }

    /**
     * Get the effective locale for the app
     */
    @JvmStatic
    fun getEffectiveLocale(): Locale {
        val language = getSelectedLanguage()
        if (language == AppLanguage.SYSTEM || language.locale == null) {
            return Locale.getDefault()
        }
        return language.locale
    }

    @JvmStatic
    fun getOldDataMigrated(): Boolean {
        return sp.getBoolean(OLD_DATA_MIGRATED, false)
    }

    @JvmStatic
    fun setOldDataMigrated(oldDataMigrated: Boolean) {
        sp.edit().putBoolean(OLD_DATA_MIGRATED, oldDataMigrated).apply()
    }

    @JvmStatic
    fun getShowContentAlreadyRead(): Boolean {
        return sp.getBoolean(SHOW_CONTENT_ALREADY_READ, false)
    }

    @JvmStatic
    fun setShowContentAlreadyRead(showContentAlreadyRead: Boolean) {
        sp.edit().putBoolean(SHOW_CONTENT_ALREADY_READ, showContentAlreadyRead).apply()
    }

    @JvmStatic
    fun getFirstTimeRunningReader(): Boolean {
        return sp.getBoolean(FIRST_TIME_RUNNING_READER, true)
    }

    @JvmStatic
    fun setFirstTimeRunningReader(firstTimeRunningReader: Boolean) {
        sp.edit().putBoolean(FIRST_TIME_RUNNING_READER, firstTimeRunningReader).apply()
    }

    // Baidu translator methods removed in Phase 15 - only AI translator is used now

    internal fun hasKey(key: String): Boolean {
        return sp.contains(key)
    }
}
