package com.mmjang.ankihelper.ui.ai;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.google.android.material.textfield.TextInputEditText;

import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.ai.AIConfigRepository;
import com.mmjang.ankihelper.data.ai.AITranslatorConfig;
import com.mmjang.ankihelper.data.ai.LLMConfig;

import java.util.List;

public class AITranslatorConfigEditorActivity extends AppCompatActivity {
  private TextInputEditText editTextTranslatorName;
  private Spinner spinnerLLM;
  private CheckBox checkBoxDefault;
  private Spinner spinnerSourceLanguage;
  private Spinner spinnerTargetLanguage;
  private AITranslatorConfig currentConfig;
  private List<LLMConfig> llmConfigs;
  private boolean isEditMode = false;

  // Common language codes and names
  private static final String[] LANGUAGE_CODES = {
      "en", "zh", "ja", "ko", "fr", "de", "es", "it", "pt", "ru", "ar", "hi"
  };

  private static final String[] LANGUAGE_NAMES = {
      "English", "Chinese", "Japanese", "Korean", "French", "German",
      "Spanish", "Italian", "Portuguese", "Russian", "Arabic", "Hindi"
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    if (com.mmjang.ankihelper.data.Settings.getInstance(this).getPinkThemeQ()) {
      setTheme(R.style.AppThemePink);
    }
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ai_translator_config_editor);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    initViews();
    loadLLMConfigs();
    loadLanguageOptions();
    handleIntent();
  }

  private void initViews() {
    editTextTranslatorName = findViewById(R.id.edit_text_translator_name);
    spinnerLLM = findViewById(R.id.spinner_llm);
    checkBoxDefault = findViewById(R.id.checkbox_default);
    spinnerSourceLanguage = findViewById(R.id.spinner_source_language);
    spinnerTargetLanguage = findViewById(R.id.spinner_target_language);
  }

  private void loadLLMConfigs() {
    llmConfigs = AIConfigRepository.getAllLLMConfigs();
    String[] llmNames = new String[llmConfigs.size()];
    for (int i = 0; i < llmConfigs.size(); i++) {
      llmNames[i] = llmConfigs.get(i).getName();
    }

    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, llmNames);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerLLM.setAdapter(adapter);
  }

  private void loadLanguageOptions() {
    ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
        LANGUAGE_NAMES);
    languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    spinnerSourceLanguage.setAdapter(languageAdapter);
    spinnerTargetLanguage.setAdapter(languageAdapter);

    // Set default selections
    spinnerSourceLanguage.setSelection(0); // English
    spinnerTargetLanguage.setSelection(1); // Chinese
  }

  private void handleIntent() {
    long configId = getIntent().getLongExtra("translator_config_id", -1);
    if (configId != -1) {
      currentConfig = AIConfigRepository.getAITranslatorConfigById(configId);
      if (currentConfig != null) {
        isEditMode = true;
        populateFields();
      }
    } else {
      // Set default prompt for new configurations
    }
  }

  private void populateFields() {
    editTextTranslatorName.setText(currentConfig.getTranslatorName());
    checkBoxDefault.setChecked(currentConfig.isDefault());

    // Select the correct LLM in the spinner
    for (int i = 0; i < llmConfigs.size(); i++) {
      if (llmConfigs.get(i).getId() == currentConfig.getLlmId()) {
        spinnerLLM.setSelection(i);
        break;
      }
    }

    // Select the correct languages in the spinners
    String sourceLanguage = currentConfig.getSourceLanguage();
    String targetLanguage = currentConfig.getTargetLanguage();

    if (sourceLanguage != null && !sourceLanguage.isEmpty()) {
      for (int i = 0; i < LANGUAGE_CODES.length; i++) {
        if (LANGUAGE_CODES[i].equals(sourceLanguage)) {
          spinnerSourceLanguage.setSelection(i);
          break;
        }
      }
    }

    if (targetLanguage != null && !targetLanguage.isEmpty()) {
      for (int i = 0; i < LANGUAGE_CODES.length; i++) {
        if (LANGUAGE_CODES[i].equals(targetLanguage)) {
          spinnerTargetLanguage.setSelection(i);
          break;
        }
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
    String translatorName = editTextTranslatorName.getText().toString().trim();
    boolean isDefault = checkBoxDefault.isChecked();
    String sourceLanguage = LANGUAGE_CODES[spinnerSourceLanguage.getSelectedItemPosition()];
    String targetLanguage = LANGUAGE_CODES[spinnerTargetLanguage.getSelectedItemPosition()];

    if (TextUtils.isEmpty(translatorName)) {
      editTextTranslatorName.setError("Translator name is required");
      return;
    }

    if (spinnerLLM.getSelectedItem() == null) {
      Toast.makeText(this, "Please select an LLM", Toast.LENGTH_SHORT).show();
      return;
    }

    if (currentConfig == null) {
      currentConfig = new AITranslatorConfig();
    }

    currentConfig.setTranslatorName(translatorName);
    currentConfig.setLlmId(llmConfigs.get(spinnerLLM.getSelectedItemPosition()).getId());
    currentConfig.setDefault(isDefault);
    currentConfig.setSourceLanguage(sourceLanguage);
    currentConfig.setTargetLanguage(targetLanguage);

    AIConfigRepository.saveAITranslatorConfig(currentConfig);
    Toast.makeText(this, "Configuration saved successfully", Toast.LENGTH_SHORT).show();
    finish();
  }
}