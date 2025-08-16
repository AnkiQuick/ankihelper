package com.mmjang.ankihelper.ui.ai;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.mmjang.ankihelper.R;

public class AIConfigActivity extends AppCompatActivity {
    private Button btnManageLLM;
    private Button btnManageTTS;
    private Button btnManageAIDictionary;
    private Button btnManageAITranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_config);

        // Set up the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("AI Configuration");
        }

        initViews();
        setListeners();
    }

    private void initViews() {
        // Initialize views
        btnManageLLM = findViewById(R.id.btn_manage_llm);
        btnManageTTS = findViewById(R.id.btn_manage_tts);
        btnManageAIDictionary = findViewById(R.id.btn_manage_ai_dictionary);
        btnManageAITranslator = findViewById(R.id.btn_manage_ai_translator);
    }

    private void setListeners() {
        // Set click listeners
        btnManageLLM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AIConfigActivity.this, LLMConfigListActivity.class);
                startActivity(intent);
            }
        });

        btnManageTTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AIConfigActivity.this, TTSConfigListActivity.class);
                startActivity(intent);
            }
        });

        btnManageAIDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AIConfigActivity.this, AIDictionaryConfigListActivity.class);
                startActivity(intent);
            }
        });

        btnManageAITranslator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AIConfigActivity.this, AITranslatorConfigListActivity.class);
                startActivity(intent);
            }
        });
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