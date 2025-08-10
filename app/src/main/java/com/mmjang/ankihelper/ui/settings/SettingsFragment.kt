package com.mmjang.ankihelper.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import com.mmjang.ankihelper.R
import com.mmjang.ankihelper.data.Settings

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

        // Restart activity to apply theme change
        activity?.recreate()
      }
      "default_tag" -> {
        legacySettings.setDefaultTag(sharedPreferences?.getString(key, "") ?: "")
      }
      "show_read_content" -> {
        legacySettings.setShowContentAlreadyRead(sharedPreferences?.getBoolean(key, false) ?: false)
      }
      "pronounce_language" -> {
        val languageIndex = sharedPreferences?.getString(key, "0")?.toInt() ?: 0
        legacySettings.setLastPronounceLanguage(languageIndex)
      }
    }
  }
}
