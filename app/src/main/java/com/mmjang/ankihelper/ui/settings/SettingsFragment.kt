package com.mmjang.ankihelper.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import com.mmjang.ankihelper.R
import com.mmjang.ankihelper.data.Settings
import com.mmjang.ankihelper.data.ThemeManager

class SettingsFragment :
        PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.preferences, rootKey)
  }

  override fun onStart() {
    super.onStart()
    preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
  }

  override fun onStop() {
    super.onStop()
    preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    key ?: return

    // Sync with legacy Settings singleton
    val context = requireContext()
    val legacySettings = Settings.getInstance(context)

    when (key) {
      "monitor_clipboard" -> {
        legacySettings.setMoniteClipboardQ(sharedPreferences?.getBoolean(key, false) ?: false)
      }
      "auto_cancel_popup" -> {
        legacySettings.setAutoCancelPopupQ(sharedPreferences?.getBoolean(key, false) ?: false)
      }
      "left_hand_mode" -> {
        legacySettings.setLeftHandModeQ(sharedPreferences?.getBoolean(key, false) ?: false)
      }
      "pink_theme" -> {
        val enabled = sharedPreferences?.getBoolean(key, false) ?: false
        legacySettings.setPinkThemeQ(enabled)

        // Apply theme change using modern approach
        activity?.let { ThemeManager.applyThemeDynamically(it) }
      }
      "pronounce_language" -> {
        val languageIndex = sharedPreferences?.getString(key, "0")?.toInt() ?: 0
        legacySettings.setLastPronounceLanguage(languageIndex)
      }
    }
  }
}
