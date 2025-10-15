package com.lmyby.ankihelper.ui.ai;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.lmyby.ankihelper.R;
import com.lmyby.ankihelper.data.Settings;
import com.lmyby.ankihelper.data.ai.AIConfigRepository;
import com.lmyby.ankihelper.data.ai.cache.AICacheRepository;

public class CleanAIHistoryActivity extends AppCompatActivity {

    private EditText mEditTextAIDictRecords;
    private EditText mEditTextAITransRecords;
    private MaterialSwitch mSwitchCleanAllAIDict;
    private MaterialSwitch mSwitchCleanAllAITrans;
    private Button mBtnCleanAIDict;
    private Button mBtnCleanAITrans;
    private TextView mTextViewAIDictTotalRecords;
    private TextView mTextViewAITransTotalRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean_ai_history);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_clean_ai_history);
        }

        assignViews();
        updateTotalRecords();
        setEventListeners();
    }

    private void assignViews() {
        mEditTextAIDictRecords = findViewById(R.id.edit_text_ai_dict_records);
        mEditTextAITransRecords = findViewById(R.id.edit_text_ai_trans_records);
        mSwitchCleanAllAIDict = findViewById(R.id.switch_clean_all_ai_dict);
        mSwitchCleanAllAITrans = findViewById(R.id.switch_clean_all_ai_trans);
        mBtnCleanAIDict = findViewById(R.id.btn_clean_ai_dict);
        mBtnCleanAITrans = findViewById(R.id.btn_clean_ai_trans);
        mTextViewAIDictTotalRecords = findViewById(R.id.text_view_ai_dict_total_records);
        mTextViewAITransTotalRecords = findViewById(R.id.text_view_ai_trans_total_records);
    }

    private void updateTotalRecords() {
        // Update AI Dictionary total records
        int aiDictCount = AICacheRepository.getDictionaryCacheCount();
        mTextViewAIDictTotalRecords.setText(String.valueOf(aiDictCount));

        // Update AI Translator total records
        int aiTransCount = AICacheRepository.getTranslatorCacheCount();
        mTextViewAITransTotalRecords.setText(String.valueOf(aiTransCount));
    }

    private void setEventListeners() {
        // AI Dictionary section
        mSwitchCleanAllAIDict.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditTextAIDictRecords.setEnabled(!isChecked);
            }
        });

        mBtnCleanAIDict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCleanAIDictionary();
            }
        });

        // AI Translator section
        mSwitchCleanAllAITrans.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditTextAITransRecords.setEnabled(!isChecked);
            }
        });

        mBtnCleanAITrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCleanAITranslator();
            }
        });
    }

    private void handleCleanAIDictionary() {
        if (mSwitchCleanAllAIDict.isChecked()) {
            // Clean all AI dictionary records
            showConfirmDialog("AI Dictionary", "all", true, 0);
        } else {
            // Clean specific number of AI dictionary records
            String inputText = mEditTextAIDictRecords.getText().toString();
            if (inputText.isEmpty()) {
                Toast.makeText(this, "Please enter a number of records to clean", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int recordCount = Integer.parseInt(inputText);
                if (recordCount < 0) {
                    Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                showConfirmDialog("AI Dictionary", String.valueOf(recordCount), false, recordCount);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleCleanAITranslator() {
        if (mSwitchCleanAllAITrans.isChecked()) {
            // Clean all AI translator records
            showConfirmDialog("AI Translator", "all", true, 0);
        } else {
            // Clean specific number of AI translator records
            String inputText = mEditTextAITransRecords.getText().toString();
            if (inputText.isEmpty()) {
                Toast.makeText(this, "Please enter a number of records to clean", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int recordCount = Integer.parseInt(inputText);
                if (recordCount < 0) {
                    Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                showConfirmDialog("AI Translator", String.valueOf(recordCount), false, recordCount);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showConfirmDialog(String type, String count, boolean cleanAll, int recordCount) {
        String message = "Are you sure you want to clean " + count + " records from " + type + "?";
        if (cleanAll) {
            message = "Are you sure you want to clean ALL records from " + type + "?";
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Clean")
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (type.equals("AI Dictionary")) {
                        performCleanAIDictionary(cleanAll, recordCount);
                    } else {
                        performCleanAITranslator(cleanAll, recordCount);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performCleanAIDictionary(boolean cleanAll, int recordCount) {
        try {
            int deletedCount;
            if (cleanAll) {
                deletedCount = AIConfigRepository.deleteAllAIDictionaryCache();
            } else {
                deletedCount = AIConfigRepository.deleteOldestAIDictionaryCache(recordCount);
            }
            Toast.makeText(this, "Successfully cleaned " + deletedCount + " AI Dictionary records", Toast.LENGTH_LONG).show();
            updateTotalRecords(); // Update the total records display
        } catch (Exception e) {
            Toast.makeText(this, "Failed to clean AI Dictionary records: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void performCleanAITranslator(boolean cleanAll, int recordCount) {
        try {
            int deletedCount;
            if (cleanAll) {
                deletedCount = AIConfigRepository.deleteAllAITranslatorCache();
            } else {
                deletedCount = AIConfigRepository.deleteOldestAITranslatorCache(recordCount);
            }
            Toast.makeText(this, "Successfully cleaned " + deletedCount + " AI Translator records", Toast.LENGTH_LONG).show();
            updateTotalRecords(); // Update the total records display
        } catch (Exception e) {
            Toast.makeText(this, "Failed to clean AI Translator records: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}