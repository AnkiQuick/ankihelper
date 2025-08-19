package com.mmjang.ankihelper.ui.ai;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.ai.AIConfigRepository;
import com.mmjang.ankihelper.data.ai.AIDictionaryConfig;

import java.util.List;

public class AIDictionaryConfigListActivity extends AppCompatActivity implements AIDictionaryConfigAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private AIDictionaryConfigAdapter adapter;
    private List<AIDictionaryConfig> dictionaryConfigList;
    private FloatingActionButton fabAddDictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (com.mmjang.ankihelper.data.Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_dictionary_config_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
        loadDictionaryConfigs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDictionaryConfigs(); // Refresh the list when returning from editor
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_dictionary_configs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAddDictionary = findViewById(R.id.fab_add_dictionary);
        fabAddDictionary.setOnClickListener(v -> {
            Intent intent = new Intent(AIDictionaryConfigListActivity.this, AIDictionaryConfigEditorActivity.class);
            startActivity(intent);
        });
    }

    private void loadDictionaryConfigs() {
        dictionaryConfigList = AIConfigRepository.getAllAIDictionaryConfigs();
        adapter = new AIDictionaryConfigAdapter(dictionaryConfigList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AIDictionaryConfig config) {
        Intent intent = new Intent(AIDictionaryConfigListActivity.this, AIDictionaryConfigEditorActivity.class);
        intent.putExtra("dictionary_config_id", config.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(AIDictionaryConfig config) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Configuration")
                .setMessage("Are you sure you want to delete \"" + config.getDictionaryName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the configuration
                    AIConfigRepository.deleteAIDictionaryConfig(config.getId());
                    // Refresh the list
                    loadDictionaryConfigs();
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