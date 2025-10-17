package com.lmyby.ankihelper.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.lmyby.ankihelper.MyApplication
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.anki.AnkiDroidHelper
import com.lmyby.ankihelper.data.AppLanguage
import com.lmyby.ankihelper.data.AppTheme
import com.lmyby.ankihelper.data.LanguageManager
import com.lmyby.ankihelper.data.Settings
import com.lmyby.ankihelper.data.ThemeManager
import com.lmyby.ankihelper.domain.CBWatcherService
import com.lmyby.ankihelper.ui.ai.AIConfigActivity
import com.lmyby.ankihelper.ui.plan.PlansManagerActivity
import com.lmyby.ankihelper.ui.settings.SettingsActivity
import com.lmyby.ankihelper.ui.stat.StatActivity
import com.lmyby.ankihelper.ui.storage.StorageMigrationActivity
import com.lmyby.ankihelper.util.StorageManager

class LauncherActivity : AppCompatActivity() {

    private lateinit var mAnkiDroid: AnkiDroidHelper
    private lateinit var settings: Settings

    // Views
    private lateinit var switchMoniteClipboard: MaterialSwitch
    private lateinit var switchCancelAfterAdd: MaterialSwitch
    private lateinit var switchLeftHandMode: MaterialSwitch
    private lateinit var themeSpinner: Spinner
    private lateinit var languageSpinner: Spinner
    private lateinit var textViewOpenPlanManager: TextView
    private lateinit var textViewOpenAIConfig: TextView
    private lateinit var textViewAcknowledge: TextView
    private lateinit var textViewOpenStatistics: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = Settings.getInstance(this@LauncherActivity)
        ThemeManager.applyTheme(this)
        // Language is now handled automatically by AndroidX AppCompatDelegate
        // Sync Settings with the current app language on startup
        LanguageManager.syncWithSettings(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher) // Set the layout first
        setVersion()

        // Check for storage migration requirement
        checkStorageMigration()

        // Initialize AnkiDroidHelper in onCreate
        mAnkiDroid = MyApplication.getAnkiDroid(this)

        // Call checkAndRequestPermissions after AnkiDroidHelper is initialized
        checkAndRequestPermissions()

        // Initialize views
        switchMoniteClipboard = findViewById(R.id.switch_monite_clipboard)
        switchCancelAfterAdd = findViewById(R.id.switch_cancel_after_add)
        switchLeftHandMode = findViewById(R.id.left_hand_mode)
        themeSpinner = findViewById(R.id.theme_spinner)
        languageSpinner = findViewById(R.id.language_spinner)
        textViewOpenPlanManager = findViewById(R.id.btn_open_plan_manager)
        textViewOpenAIConfig = findViewById(R.id.btn_open_ai_config)
        textViewAcknowledge = findViewById(R.id.textview_acknowledge)
        textViewOpenStatistics = findViewById(R.id.btn_open_statistics)

        switchMoniteClipboard.isChecked = settings.getMoniteClipboardQ()
        switchCancelAfterAdd.isChecked = settings.getAutoCancelPopupQ()
        switchLeftHandMode.isChecked = settings.getLeftHandModeQ()

        // Setup theme spinner
        setupThemeSpinner()

        // Setup language spinner
        setupLanguageSpinner()

        switchMoniteClipboard.setOnCheckedChangeListener { _, isChecked ->
            settings.setMoniteClipboardQ(isChecked)
            if (isChecked) {
                startCBService()
            } else {
                stopCBService()
            }
        }

        switchLeftHandMode.setOnCheckedChangeListener { _, isChecked ->
            settings.setLeftHandModeQ(isChecked)
        }

        switchCancelAfterAdd.setOnCheckedChangeListener { _, isChecked ->
            settings.setAutoCancelPopupQ(isChecked)
        }

        textViewOpenPlanManager.setOnClickListener {
            if (!mAnkiDroid.isAnkiDroidRunning) {
                Toast.makeText(
                    this@LauncherActivity,
                    R.string.api_not_available_message,
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (mAnkiDroid.shouldRequestPermission()) {
                mAnkiDroid.requestPermission(this@LauncherActivity, 0)
                return@setOnClickListener
            }

            val intent = Intent(this@LauncherActivity, PlansManagerActivity::class.java)
            startActivity(intent)
        }

        textViewOpenAIConfig.setOnClickListener {
            val intent = Intent(this@LauncherActivity, AIConfigActivity::class.java)
            startActivity(intent)
        }

        textViewOpenStatistics.setOnClickListener {
            val intent = Intent(this@LauncherActivity, StatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkStorageMigration() {
        val storageManager = StorageManager(this, getSharedPreferences("ankihelper_prefs", MODE_PRIVATE))

        // Check if migration is needed
        if (!storageManager.isMigrationCompleted()) {
            val intent = Intent(this, StorageMigrationActivity::class.java)
            startActivity(intent)
            finish() // Close launcher activity
        }
    }

    private fun checkAndRequestPermissions() {
        if (mAnkiDroid.shouldRequestPermission()) {
            mAnkiDroid.requestPermission(this, REQUEST_CODE_ANKI)
        }
        // Only check notification permission (for internal storage)
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_ANKI
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Call super.onRequestPermissionsResult() first
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isEmpty()) {
            return
        }

        if (requestCode == REQUEST_CODE_ANKI) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ensureExternalDbDirectoryAndMigrate()
            } else {
                AlertDialog.Builder(this@LauncherActivity)
                    .setMessage(R.string.permission_denied)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { _: DialogInterface, _: Int ->
                        openSettingsPage()
                    }
                    .show()
            }
        }
        // The REQUEST_CODE_STORAGE is no longer used for internal storage
    }

    private fun ensureExternalDbDirectoryAndMigrate() {
        // Check for storage permission
        // This function is no longer needed, as we are using internal storage now
    }

    private fun startCBService() {
        val intent = Intent(this, CBWatcherService::class.java)
        startService(intent)
    }

    private fun stopCBService() {
        val intent = Intent(this, CBWatcherService::class.java)
        stopService(intent)
    }

    private fun showNoPlansGuidance() {
        AlertDialog.Builder(this)
            .setTitle(R.string.no_plans_found_title)
            .setMessage(R.string.no_plans_found_message)
            .setPositiveButton(R.string.go_to_plan_manager) { _, _ ->
                val intent = Intent(this, PlansManagerActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun openSettingsPage() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Setup theme selection spinner
     */
    private fun setupThemeSpinner() {
        // Create adapter with theme display names
        val themeNames = Array(AppTheme.values().size) { i ->
            AppTheme.values()[i].getDisplayName(this)
        }

        val themeAdapter = ArrayAdapter(
            this,
            R.layout.centered_spinner_item,
            themeNames
        )
        themeAdapter.setDropDownViewResource(R.layout.centered_spinner_dropdown_item)
        themeSpinner.adapter = themeAdapter

        // Set current selection
        val currentTheme = settings.getSelectedTheme()
        themeSpinner.setSelection(currentTheme.ordinal)

        // Handle theme selection changes
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedTheme = AppTheme.values()[position]
                val currentTheme = settings.getSelectedTheme()

                if (selectedTheme != currentTheme) {
                    settings.setSelectedTheme(selectedTheme)
                    applyThemeWithoutRestart()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    /**
     * Apply theme changes without requiring app restart
     * Uses modern Android practices for dynamic theme switching
     */
    private fun applyThemeWithoutRestart() {
        // Get the new theme
        val newTheme = settings.getSelectedTheme()

        // Apply theme to the current activity
        setTheme(newTheme.themeResId)

        // Recreate the activity to apply the new theme
        recreate()
    }

    /**
     * Setup language selection spinner
     */
    private fun setupLanguageSpinner() {
        // Create adapter with language display names
        val languageNames = Array(AppLanguage.values().size) { i ->
            AppLanguage.values()[i].getDisplayName(this)
        }

        val languageAdapter = ArrayAdapter(
            this,
            R.layout.centered_spinner_item,
            languageNames
        )
        languageAdapter.setDropDownViewResource(R.layout.centered_spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter

        // Set current selection
        val currentLanguage = settings.getSelectedLanguage()
        languageSpinner.setSelection(currentLanguage.ordinal)

        // Handle language selection changes
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLanguage = AppLanguage.values()[position]
                val currentLanguage = settings.getSelectedLanguage()

                if (selectedLanguage != currentLanguage) {
                    settings.setSelectedLanguage(selectedLanguage)
                    applyLanguageWithoutRestart()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    /**
     * Apply language changes using modern AndroidX API
     * The API will automatically recreate all activities with the new language
     */
    private fun applyLanguageWithoutRestart() {
        val selectedLanguage = settings.getSelectedLanguage()

        // Use modern AndroidX API - this will:
        // 1. Persist the language preference automatically
        // 2. Recreate all activities with the new language
        // 3. Update the app name and all system UI
        LanguageManager.setAppLanguage(selectedLanguage)

        // No need to manually recreate - AppCompatDelegate handles it
    }

    private fun setVersion() {
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            val versionTextView = findViewById<TextView>(R.id.textview_version)
            versionTextView.text = "Ver: $versionName"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val REQUEST_CODE_ANKI = 0
    }
}
