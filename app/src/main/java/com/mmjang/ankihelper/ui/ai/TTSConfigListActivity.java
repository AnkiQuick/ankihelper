package com.mmjang.ankihelper.ui.ai;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.ai.AIConfigRepository;
import com.mmjang.ankihelper.data.ai.TTSConfig;

import java.util.List;

public class TTSConfigListActivity extends AppCompatActivity implements TTSConfigAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private TTSConfigAdapter adapter;
    private List<TTSConfig> ttsConfigList;
    private FloatingActionButton fabAddTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts_config_list);

        // Set up the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("TTS Configurations");
        }

        initViews();
        loadTTSConfigs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTTSConfigs(); // Refresh the list when returning from editor
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_tts_configs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAddTTS = findViewById(R.id.fab_add_tts);
        fabAddTTS.setOnClickListener(v -> {
            Intent intent = new Intent(TTSConfigListActivity.this, TTSConfigEditorActivity.class);
            startActivity(intent);
        });
    }

    private void loadTTSConfigs() {
        ttsConfigList = AIConfigRepository.getAllTTSConfigs();
        adapter = new TTSConfigAdapter(ttsConfigList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(TTSConfig config) {
        Intent intent = new Intent(TTSConfigListActivity.this, TTSConfigEditorActivity.class);
        intent.putExtra("tts_config_id", config.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(TTSConfig config) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Configuration")
                .setMessage("Are you sure you want to delete \"" + config.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the configuration
                    AIConfigRepository.deleteTTSConfig(config.getId());
                    // Refresh the list
                    loadTTSConfigs();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}