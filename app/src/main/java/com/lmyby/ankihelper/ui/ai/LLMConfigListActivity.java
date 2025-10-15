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
import com.lmyby.ankihelper.data.ai.LLMConfig;

import java.util.List;

public class LLMConfigListActivity extends AppCompatActivity implements LLMConfigAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private LLMConfigAdapter adapter;
    private List<LLMConfig> llmConfigList;
    private FloatingActionButton fabAddLLM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (com.lmyby.ankihelper.data.Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llm_config_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
        loadLLMConfigs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLLMConfigs(); // Refresh the list when returning from editor
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_llm_configs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAddLLM = findViewById(R.id.fab_add_llm);
        fabAddLLM.setOnClickListener(v -> {
            Intent intent = new Intent(LLMConfigListActivity.this, LLMConfigEditorActivity.class);
            startActivity(intent);
        });
    }

    private void loadLLMConfigs() {
        llmConfigList = AIConfigRepository.getAllLLMConfigs();
        adapter = new LLMConfigAdapter(llmConfigList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(LLMConfig config) {
        Intent intent = new Intent(LLMConfigListActivity.this, LLMConfigEditorActivity.class);
        intent.putExtra("llm_config_id", config.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(LLMConfig config) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Configuration")
                .setMessage("Are you sure you want to delete \"" + config.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the configuration
                    AIConfigRepository.deleteLLMConfig(config.getId());
                    // Refresh the list
                    loadLLMConfigs();
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