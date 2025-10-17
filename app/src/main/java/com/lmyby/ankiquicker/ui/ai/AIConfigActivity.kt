package com.lmyby.ankiquicker.ui.ai

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.Settings

/**
 * Main activity for managing all AI-related configurations
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class AIConfigActivity : AppCompatActivity() {

    private lateinit var btnManageLLM: Button
    private lateinit var btnManageTTS: Button
    private lateinit var btnManageAIDictionary: Button
    private lateinit var btnManageAITranslator: Button
    private lateinit var btnManageAIHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_config)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        setListeners()
    }

    private fun initViews() {
        // Initialize views
        btnManageLLM = findViewById(R.id.btn_manage_llm)
        btnManageTTS = findViewById(R.id.btn_manage_tts)
        btnManageAIDictionary = findViewById(R.id.btn_manage_ai_dictionary)
        btnManageAITranslator = findViewById(R.id.btn_manage_ai_translator)
        btnManageAIHistory = findViewById(R.id.btn_manage_ai_history)
    }

    private fun setListeners() {
        // Set click listeners
        btnManageLLM.setOnClickListener {
            val intent = Intent(this@AIConfigActivity, LLMConfigListActivity::class.java)
            startActivity(intent)
        }

        btnManageTTS.setOnClickListener {
            val intent = Intent(this@AIConfigActivity, TTSConfigListActivity::class.java)
            startActivity(intent)
        }

        btnManageAIDictionary.setOnClickListener {
            val intent = Intent(this@AIConfigActivity, AIDictionaryConfigListActivity::class.java)
            startActivity(intent)
        }

        btnManageAITranslator.setOnClickListener {
            val intent = Intent(this@AIConfigActivity, AITranslatorConfigListActivity::class.java)
            startActivity(intent)
        }

        btnManageAIHistory.setOnClickListener {
            val intent = Intent(this@AIConfigActivity, CleanAIHistoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
