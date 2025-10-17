package com.lmyby.ankihelper.ui.ai

import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.app.NavUtils
import com.google.android.material.textfield.TextInputEditText
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.data.Settings
import com.lmyby.ankihelper.data.ai.AIConfigRepository
import com.lmyby.ankihelper.data.ai.LLMConfig

/**
 * Activity for editing LLM configuration
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class LLMConfigEditorActivity : AppCompatActivity() {

    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextBaseUrl: TextInputEditText
    private lateinit var editTextApiToken: TextInputEditText
    private lateinit var edittextModelName: TextInputEditText
    private lateinit var spinnerProvider: AppCompatSpinner

    private var currentConfig: LLMConfig? = null
    private var isEditMode = false

    // Predefined providers with their base URLs
    private val providerUrls = mutableMapOf<String, String>()

    // List of provider names for the spinner
    private val providerNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_llm_config_editor)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initProviderData()
        initViews()
        setupProviderSpinner()
        handleIntent()
    }

    private fun initProviderData() {
        // Initialize predefined providers and their URLs
        providerUrls[getString(R.string.provider_custom)] = ""
        providerUrls[getString(R.string.provider_deepseek)] = "https://api.deepseek.com"
        providerUrls[getString(R.string.provider_openai)] = "https://api.openai.com"
        providerUrls[getString(R.string.provider_aliyun)] = "https://dashscope.aliyuncs.com"

        // Create list of provider names
        providerNames.addAll(providerUrls.keys)
    }

    private fun initViews() {
        editTextName = findViewById(R.id.edit_text_name)
        editTextBaseUrl = findViewById(R.id.edit_text_base_url)
        editTextApiToken = findViewById(R.id.edit_text_api_token)
        edittextModelName = findViewById(R.id.edit_text_model_name)
        spinnerProvider = findViewById(R.id.spinner_provider)
    }

    private fun setupProviderSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, providerNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvider.adapter = adapter

        spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateBaseUrlField(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun updateBaseUrlField(position: Int) {
        val selectedProvider = providerNames[position]
        val baseUrl = providerUrls[selectedProvider]

        if (selectedProvider == getString(R.string.provider_custom)) {
            // Enable editing for custom provider
            editTextBaseUrl.isEnabled = true
            // Don't overwrite existing custom URL
        } else {
            // Set predefined URL and disable editing for known providers
            editTextBaseUrl.isEnabled = false
            editTextBaseUrl.setText(baseUrl)
        }
    }

    private fun findProviderByBaseUrl(baseUrl: String?): String {
        // First check for exact match
        for ((key, value) in providerUrls) {
            if (value == baseUrl) {
                return key
            }
        }

        // If no exact match, check if it's a custom URL
        if (!baseUrl.isNullOrEmpty()) {
            return getString(R.string.provider_custom)
        }

        // Default to custom if no URL
        return getString(R.string.provider_custom)
    }

    private fun handleIntent() {
        val configId = intent.getLongExtra("llm_config_id", -1)
        if (configId != -1L) {
            currentConfig = AIConfigRepository.getLLMConfigById(configId)
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

            // Set the provider spinner based on the base URL
            val baseUrl = config.baseUrl
            val providerName = findProviderByBaseUrl(baseUrl)
            val position = providerNames.indexOf(providerName)
            if (position >= 0) {
                spinnerProvider.setSelection(position)
                // Update the base URL field based on provider selection
                updateBaseUrlField(position)
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

        val config = currentConfig ?: LLMConfig()

        config.name = name
        config.baseUrl = baseUrl
        if (!TextUtils.isEmpty(apiToken)) {
            config.apiToken = apiToken
        }
        config.modelName = modelName

        AIConfigRepository.saveLLMConfig(config)
        Toast.makeText(this, "Configuration saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
