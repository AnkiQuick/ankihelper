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
import com.lmyby.ankihelper.data.ai.AIDictionaryConfig

/**
 * Activity for managing AI dictionary configuration list
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class AIDictionaryConfigListActivity : AppCompatActivity(), AIDictionaryConfigAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AIDictionaryConfigAdapter
    private lateinit var dictionaryConfigList: List<AIDictionaryConfig>
    private lateinit var fabAddDictionary: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_dictionary_config_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        loadDictionaryConfigs()
    }

    override fun onResume() {
        super.onResume()
        loadDictionaryConfigs() // Refresh the list when returning from editor
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view_dictionary_configs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAddDictionary = findViewById(R.id.fab_add_dictionary)
        fabAddDictionary.setOnClickListener {
            val intent = Intent(this@AIDictionaryConfigListActivity, AIDictionaryConfigEditorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadDictionaryConfigs() {
        dictionaryConfigList = AIConfigRepository.getAllAIDictionaryConfigs()
        adapter = AIDictionaryConfigAdapter(dictionaryConfigList, this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(config: AIDictionaryConfig) {
        val intent = Intent(this@AIDictionaryConfigListActivity, AIDictionaryConfigEditorActivity::class.java)
        intent.putExtra("dictionary_config_id", config.id)
        startActivity(intent)
    }

    override fun onDeleteClick(config: AIDictionaryConfig) {
        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Configuration")
            .setMessage("Are you sure you want to delete \"${config.dictionaryName}\"?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete the configuration
                AIConfigRepository.deleteAIDictionaryConfig(config.id)
                // Refresh the list
                loadDictionaryConfigs()
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
