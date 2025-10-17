package com.lmyby.ankihelper.ui.ai

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.data.Settings
import com.lmyby.ankihelper.data.ai.AIConfigRepository
import com.lmyby.ankihelper.data.ai.LLMConfig

/**
 * Activity for managing LLM configuration list
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class LLMConfigListActivity : AppCompatActivity(), LLMConfigAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LLMConfigAdapter
    private lateinit var llmConfigList: List<LLMConfig>
    private lateinit var fabAddLLM: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_llm_config_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        loadLLMConfigs()
    }

    override fun onResume() {
        super.onResume()
        loadLLMConfigs() // Refresh the list when returning from editor
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view_llm_configs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAddLLM = findViewById(R.id.fab_add_llm)
        fabAddLLM.setOnClickListener {
            val intent = Intent(this@LLMConfigListActivity, LLMConfigEditorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadLLMConfigs() {
        llmConfigList = AIConfigRepository.getAllLLMConfigs()
        adapter = LLMConfigAdapter(llmConfigList, this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(config: LLMConfig) {
        val intent = Intent(this@LLMConfigListActivity, LLMConfigEditorActivity::class.java)
        intent.putExtra("llm_config_id", config.id)
        startActivity(intent)
    }

    override fun onDeleteClick(config: LLMConfig) {
        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Configuration")
            .setMessage("Are you sure you want to delete \"${config.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete the configuration
                AIConfigRepository.deleteLLMConfig(config.id)
                // Refresh the list
                loadLLMConfigs()
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
