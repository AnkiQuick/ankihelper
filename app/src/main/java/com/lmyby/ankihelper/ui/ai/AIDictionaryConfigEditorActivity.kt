package com.lmyby.ankihelper.ui.ai

import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.google.android.material.textfield.TextInputEditText
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.data.Settings
import com.lmyby.ankihelper.data.ai.AIConfigRepository
import com.lmyby.ankihelper.data.ai.AIDictionaryConfig
import com.lmyby.ankihelper.data.ai.LLMConfig

/**
 * Activity for editing AI dictionary configuration
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class AIDictionaryConfigEditorActivity : AppCompatActivity() {

    private lateinit var editTextDictionaryName: TextInputEditText
    private lateinit var spinnerLLM: Spinner
    private lateinit var spinnerSourceLanguage: Spinner
    private lateinit var spinnerTargetLanguage: Spinner
    private var currentConfig: AIDictionaryConfig? = null
    private lateinit var llmConfigs: List<LLMConfig>
    private var isEditMode = false

    companion object {
        // Common language codes and names
        private val LANGUAGE_CODES = arrayOf(
            "en", "zh", "ja", "ko", "fr", "de", "es", "it", "pt", "ru", "ar", "hi"
        )

        private val LANGUAGE_NAMES = arrayOf(
            "English", "Chinese", "Japanese", "Korean", "French", "German",
            "Spanish", "Italian", "Portuguese", "Russian", "Arabic", "Hindi"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_dictionary_config_editor)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        loadLLMConfigs()
        loadLanguageOptions()
        handleIntent()
    }

    private fun initViews() {
        editTextDictionaryName = findViewById(R.id.edit_text_dictionary_name)
        spinnerLLM = findViewById(R.id.spinner_llm)
        spinnerSourceLanguage = findViewById(R.id.spinner_source_language)
        spinnerTargetLanguage = findViewById(R.id.spinner_target_language)
    }

    private fun loadLLMConfigs() {
        llmConfigs = AIConfigRepository.getAllLLMConfigs()
        val llmNames = llmConfigs.map { it.name }.toTypedArray()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, llmNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLLM.adapter = adapter
    }

    private fun loadLanguageOptions() {
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, LANGUAGE_NAMES)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerSourceLanguage.adapter = languageAdapter
        spinnerTargetLanguage.adapter = languageAdapter

        // Set default selections
        spinnerSourceLanguage.setSelection(0) // English
        spinnerTargetLanguage.setSelection(1) // Chinese
    }

    private fun handleIntent() {
        val configId = intent.getLongExtra("dictionary_config_id", -1)
        if (configId != -1L) {
            currentConfig = AIConfigRepository.getAIDictionaryConfigById(configId)
            if (currentConfig != null) {
                isEditMode = true
                populateFields()
            }
        }
        // Set default prompt for new configurations if needed
    }

    private fun populateFields() {
        currentConfig?.let { config ->
            editTextDictionaryName.setText(config.dictionaryName)

            // Select the correct LLM in the spinner
            for (i in llmConfigs.indices) {
                if (llmConfigs[i].id == config.llmId) {
                    spinnerLLM.setSelection(i)
                    break
                }
            }

            // Select the correct languages in the spinners
            val sourceLanguage = config.sourceLanguage
            val targetLanguage = config.targetLanguage

            if (!sourceLanguage.isNullOrEmpty()) {
                for (i in LANGUAGE_CODES.indices) {
                    if (LANGUAGE_CODES[i] == sourceLanguage) {
                        spinnerSourceLanguage.setSelection(i)
                        break
                    }
                }
            }

            if (!targetLanguage.isNullOrEmpty()) {
                for (i in LANGUAGE_CODES.indices) {
                    if (LANGUAGE_CODES[i] == targetLanguage) {
                        spinnerTargetLanguage.setSelection(i)
                        break
                    }
                }
            }
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
        val dictionaryName = editTextDictionaryName.text.toString().trim()
        val sourceLanguage = LANGUAGE_CODES[spinnerSourceLanguage.selectedItemPosition]
        val targetLanguage = LANGUAGE_CODES[spinnerTargetLanguage.selectedItemPosition]

        if (TextUtils.isEmpty(dictionaryName)) {
            editTextDictionaryName.error = "Dictionary name is required"
            return
        }

        if (spinnerLLM.selectedItem == null) {
            Toast.makeText(this, "Please select an LLM", Toast.LENGTH_SHORT).show()
            return
        }

        val config = currentConfig ?: AIDictionaryConfig()

        config.dictionaryName = dictionaryName
        config.llmId = llmConfigs[spinnerLLM.selectedItemPosition].id
        config.sourceLanguage = sourceLanguage
        config.targetLanguage = targetLanguage

        AIConfigRepository.saveAIDictionaryConfig(config)
        Toast.makeText(this, "Configuration saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
