package com.mmjang.ankihelper.ui.ai;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.NavUtils;

import com.google.android.material.textfield.TextInputEditText;

import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.ai.AIConfigRepository;
import com.mmjang.ankihelper.data.ai.LLMConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLMConfigEditorActivity extends AppCompatActivity {
    private TextInputEditText editTextName;
    private TextInputEditText editTextBaseUrl;
    private TextInputEditText editTextApiToken;
    private TextInputEditText edittextModelName;
    private AppCompatSpinner spinnerProvider;
    
    private LLMConfig currentConfig;
    private boolean isEditMode = false;
    
    // Predefined providers with their base URLs
    private Map<String, String> providerUrls = new HashMap<>();
    
    // List of provider names for the spinner
    private List<String> providerNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (com.mmjang.ankihelper.data.Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llm_config_editor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initProviderData();
        initViews();
        setupProviderSpinner();
        handleIntent();
    }

    private void initProviderData() {
        // Initialize predefined providers and their URLs
        providerUrls.put(getString(R.string.provider_custom), "");
        providerUrls.put(getString(R.string.provider_deepseek), "https://api.deepseek.com");
        providerUrls.put(getString(R.string.provider_openai), "https://api.openai.com");
        providerUrls.put(getString(R.string.provider_aliyun), "https://dashscope.aliyuncs.com");
        
        // Create list of provider names
        providerNames.addAll(providerUrls.keySet());
    }

    private void initViews() {
        editTextName = findViewById(R.id.edit_text_name);
        editTextBaseUrl = findViewById(R.id.edit_text_base_url);
        editTextApiToken = findViewById(R.id.edit_text_api_token);
        edittextModelName = findViewById(R.id.edit_text_model_name);
        spinnerProvider = findViewById(R.id.spinner_provider);
    }
    
    private void setupProviderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, providerNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvider.setAdapter(adapter);
        
        spinnerProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                updateBaseUrlField(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void updateBaseUrlField(int position) {
        String selectedProvider = providerNames.get(position);
        String baseUrl = providerUrls.get(selectedProvider);
        
        if (selectedProvider.equals(getString(R.string.provider_custom))) {
            // Enable editing for custom provider
            editTextBaseUrl.setEnabled(true);
            // Don't overwrite existing custom URL
        } else {
            // Set predefined URL and disable editing for known providers
            editTextBaseUrl.setEnabled(false);
            editTextBaseUrl.setText(baseUrl);
        }
    }
    
    private String findProviderByBaseUrl(String baseUrl) {
        // First check for exact match
        for (Map.Entry<String, String> entry : providerUrls.entrySet()) {
            if (entry.getValue().equals(baseUrl)) {
                return entry.getKey();
            }
        }
        
        // If no exact match, check if it's a custom URL
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return getString(R.string.provider_custom);
        }
        
        // Default to custom if no URL
        return getString(R.string.provider_custom);
    }

    private void handleIntent() {
        long configId = getIntent().getLongExtra("llm_config_id", -1);
        if (configId != -1) {
            currentConfig = AIConfigRepository.getLLMConfigById(configId);
            if (currentConfig != null) {
                isEditMode = true;
                populateFields();
            }
        }
    }

    private void populateFields() {
        editTextName.setText(currentConfig.getName());
        editTextBaseUrl.setText(currentConfig.getBaseUrl());
        // Don't populate the API token for security reasons
        edittextModelName.setText(currentConfig.getModelName());
        
        // Set the provider spinner based on the base URL
        String baseUrl = currentConfig.getBaseUrl();
        String providerName = findProviderByBaseUrl(baseUrl);
        if (providerName != null) {
            int position = providerNames.indexOf(providerName);
            if (position >= 0) {
                spinnerProvider.setSelection(position);
                // Update the base URL field based on provider selection
                updateBaseUrlField(position);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        } else if (itemId == R.id.action_save) {
            saveConfig();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void saveConfig() {
        String name = editTextName.getText().toString().trim();
        String baseUrl = editTextBaseUrl.getText().toString().trim();
        String apiToken = editTextApiToken.getText().toString().trim();
        String modelName = edittextModelName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(baseUrl)) {
            editTextBaseUrl.setError("Base URL is required");
            return;
        }

        if (TextUtils.isEmpty(modelName)) {
            edittextModelName.setError("Model name is required");
            return;
        }

        if (currentConfig == null) {
            currentConfig = new LLMConfig();
        }

        currentConfig.setName(name);
        currentConfig.setBaseUrl(baseUrl);
        if (!TextUtils.isEmpty(apiToken)) {
            currentConfig.setApiToken(apiToken);
        }
        currentConfig.setModelName(modelName);

        AIConfigRepository.saveLLMConfig(currentConfig);
        Toast.makeText(this, "Configuration saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}