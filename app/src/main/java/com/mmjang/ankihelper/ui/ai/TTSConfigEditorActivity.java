package com.mmjang.ankihelper.ui.ai;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.google.android.material.textfield.TextInputEditText;

import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.ai.AIConfigRepository;
import com.mmjang.ankihelper.data.ai.TTSConfig;

public class TTSConfigEditorActivity extends AppCompatActivity {
    private TextInputEditText editTextName;
    private TextInputEditText editTextBaseUrl;
    private TextInputEditText editTextApiToken;
    private TextInputEditText edittextModelName;
    private TTSConfig currentConfig;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (com.mmjang.ankihelper.data.Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts_config_editor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
        handleIntent();
    }

    private void initViews() {
        editTextName = findViewById(R.id.edit_text_name);
        editTextBaseUrl = findViewById(R.id.edit_text_base_url);
        editTextApiToken = findViewById(R.id.edit_text_api_token);
        edittextModelName = findViewById(R.id.edit_text_model_name);
    }

    private void handleIntent() {
        long configId = getIntent().getLongExtra("tts_config_id", -1);
        if (configId != -1) {
            currentConfig = AIConfigRepository.getTTSConfigById(configId);
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
            currentConfig = new TTSConfig();
        }

        currentConfig.setName(name);
        currentConfig.setBaseUrl(baseUrl);
        if (!TextUtils.isEmpty(apiToken)) {
            currentConfig.setApiToken(apiToken);
        }
        currentConfig.setModelName(modelName);

        AIConfigRepository.saveTTSConfig(currentConfig);
        Toast.makeText(this, "Configuration saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}