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
import com.lmyby.ankiquicker.data.ai.AITranslatorConfig

/**
 * Activity for managing AI translator configuration list
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class AITranslatorConfigListActivity : AppCompatActivity(), AITranslatorConfigAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AITranslatorConfigAdapter
    private lateinit var translatorConfigList: List<AITranslatorConfig>
    private lateinit var fabAddTranslator: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_translator_config_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        loadTranslatorConfigs()
    }

    override fun onResume() {
        super.onResume()
        loadTranslatorConfigs() // Refresh the list when returning from editor
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view_translator_configs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAddTranslator = findViewById(R.id.fab_add_translator)
        fabAddTranslator.setOnClickListener {
            val intent = Intent(this@AITranslatorConfigListActivity, AITranslatorConfigEditorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadTranslatorConfigs() {
        translatorConfigList = AIConfigRepository.getAllAITranslatorConfigs()
        adapter = AITranslatorConfigAdapter(translatorConfigList, this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(config: AITranslatorConfig) {
        val intent = Intent(this@AITranslatorConfigListActivity, AITranslatorConfigEditorActivity::class.java)
        intent.putExtra("translator_config_id", config.id)
        startActivity(intent)
    }

    override fun onDeleteClick(config: AITranslatorConfig) {
        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Configuration")
            .setMessage("Are you sure you want to delete \"${config.translatorName}\"?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete the configuration
                AIConfigRepository.deleteAITranslatorConfig(config.id)
                // Refresh the list
                loadTranslatorConfigs()
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
