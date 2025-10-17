package com.lmyby.ankiquicker.ui.ai

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.Settings
import com.lmyby.ankiquicker.data.ai.AIConfigRepository
import com.lmyby.ankiquicker.data.ai.TTSConfig

/**
 * Activity for managing TTS configuration list
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class TTSConfigListActivity : AppCompatActivity(), TTSConfigAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TTSConfigAdapter
    private lateinit var ttsConfigList: List<TTSConfig>
    private lateinit var fabAddTTS: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tts_config_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        loadTTSConfigs()
    }

    override fun onResume() {
        super.onResume()
        loadTTSConfigs() // Refresh the list when returning from editor
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view_tts_configs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAddTTS = findViewById(R.id.fab_add_tts)
        fabAddTTS.setOnClickListener {
            val intent = Intent(this@TTSConfigListActivity, TTSConfigEditorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadTTSConfigs() {
        ttsConfigList = AIConfigRepository.getAllTTSConfigs()
        adapter = TTSConfigAdapter(ttsConfigList, this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(config: TTSConfig) {
        val intent = Intent(this@TTSConfigListActivity, TTSConfigEditorActivity::class.java)
        intent.putExtra("tts_config_id", config.id)
        startActivity(intent)
    }

    override fun onDeleteClick(config: TTSConfig) {
        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Configuration")
            .setMessage("Are you sure you want to delete \"${config.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete the configuration
                AIConfigRepository.deleteTTSConfig(config.id)
                // Refresh the list
                loadTTSConfigs()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
