package com.lmyby.ankihelper.ui.ai;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.lmyby.ankihelper.R;
import com.lmyby.ankihelper.data.ai.AIConfigRepository;
import com.lmyby.ankihelper.data.ai.AITranslatorConfig;

import java.util.List;

public class AITranslatorConfigListActivity extends AppCompatActivity implements AITranslatorConfigAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private AITranslatorConfigAdapter adapter;
    private List<AITranslatorConfig> translatorConfigList;
    private FloatingActionButton fabAddTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (com.lmyby.ankihelper.data.Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_translator_config_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
        loadTranslatorConfigs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTranslatorConfigs(); // Refresh the list when returning from editor
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_translator_configs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAddTranslator = findViewById(R.id.fab_add_translator);
        fabAddTranslator.setOnClickListener(v -> {
            Intent intent = new Intent(AITranslatorConfigListActivity.this, AITranslatorConfigEditorActivity.class);
            startActivity(intent);
        });
    }

    private void loadTranslatorConfigs() {
        translatorConfigList = AIConfigRepository.getAllAITranslatorConfigs();
        adapter = new AITranslatorConfigAdapter(translatorConfigList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AITranslatorConfig config) {
        Intent intent = new Intent(AITranslatorConfigListActivity.this, AITranslatorConfigEditorActivity.class);
        intent.putExtra("translator_config_id", config.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(AITranslatorConfig config) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Configuration")
                .setMessage("Are you sure you want to delete \"" + config.getTranslatorName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the configuration
                    AIConfigRepository.deleteAITranslatorConfig(config.getId());
                    // Refresh the list
                    loadTranslatorConfigs();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}