package com.lmyby.ankihelper.ui.ai

import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.google.android.material.textfield.TextInputEditText
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.data.Settings
import com.lmyby.ankihelper.data.ai.AIConfigRepository
import com.lmyby.ankihelper.data.ai.TTSConfig

/**
 * Activity for editing TTS configuration
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class TTSConfigEditorActivity : AppCompatActivity() {

    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextBaseUrl: TextInputEditText
    private lateinit var editTextApiToken: TextInputEditText
    private lateinit var edittextModelName: TextInputEditText
    private var currentConfig: TTSConfig? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tts_config_editor)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        handleIntent()
    }

    private fun initViews() {
        editTextName = findViewById(R.id.edit_text_name)
        editTextBaseUrl = findViewById(R.id.edit_text_base_url)
        editTextApiToken = findViewById(R.id.edit_text_api_token)
        edittextModelName = findViewById(R.id.edit_text_model_name)
    }

    private fun handleIntent() {
        val configId = intent.getLongExtra("tts_config_id", -1)
        if (configId != -1L) {
            currentConfig = AIConfigRepository.getTTSConfigById(configId)
            if (currentConfig != null) {
                isEditMode = true
                populateFields()
            }
        }
    }

    private fun populateFields() {
        currentConfig?.let { config ->
            editTextName.setText(config.name)
            editTextBaseUrl.setText(config.baseUrl)
            // Don't populate the API token for security reasons
            edittextModelName.setText(config.modelName)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            R.id.action_save -> {
                saveConfig()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveConfig() {
        val name = editTextName.text.toString().trim()
        val baseUrl = editTextBaseUrl.text.toString().trim()
        val apiToken = editTextApiToken.text.toString().trim()
        val modelName = edittextModelName.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            editTextName.error = "Name is required"
            return
        }

        if (TextUtils.isEmpty(baseUrl)) {
            editTextBaseUrl.error = "Base URL is required"
            return
        }

        if (TextUtils.isEmpty(modelName)) {
            edittextModelName.error = "Model name is required"
            return
        }

        val config = currentConfig ?: TTSConfig()

        config.name = name
        config.baseUrl = baseUrl
        if (!TextUtils.isEmpty(apiToken)) {
            config.apiToken = apiToken
        }
        config.modelName = modelName

        AIConfigRepository.saveTTSConfig(config)
        Toast.makeText(this, "Configuration saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
