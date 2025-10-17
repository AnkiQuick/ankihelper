package com.lmyby.ankihelper.ui.ai

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.data.Settings
import com.lmyby.ankihelper.data.ai.AIConfigRepository
import com.lmyby.ankihelper.data.ai.cache.AICacheRepository

/**
 * Activity for cleaning AI history (dictionary and translator caches)
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class CleanAIHistoryActivity : AppCompatActivity() {

    private lateinit var mEditTextAIDictRecords: EditText
    private lateinit var mEditTextAITransRecords: EditText
    private lateinit var mSwitchCleanAllAIDict: MaterialSwitch
    private lateinit var mSwitchCleanAllAITrans: MaterialSwitch
    private lateinit var mBtnCleanAIDict: Button
    private lateinit var mBtnCleanAITrans: Button
    private lateinit var mTextViewAIDictTotalRecords: TextView
    private lateinit var mTextViewAITransTotalRecords: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clean_ai_history)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_clean_ai_history)
        }

        assignViews()
        updateTotalRecords()
        setEventListeners()
    }

    private fun assignViews() {
        mEditTextAIDictRecords = findViewById(R.id.edit_text_ai_dict_records)
        mEditTextAITransRecords = findViewById(R.id.edit_text_ai_trans_records)
        mSwitchCleanAllAIDict = findViewById(R.id.switch_clean_all_ai_dict)
        mSwitchCleanAllAITrans = findViewById(R.id.switch_clean_all_ai_trans)
        mBtnCleanAIDict = findViewById(R.id.btn_clean_ai_dict)
        mBtnCleanAITrans = findViewById(R.id.btn_clean_ai_trans)
        mTextViewAIDictTotalRecords = findViewById(R.id.text_view_ai_dict_total_records)
        mTextViewAITransTotalRecords = findViewById(R.id.text_view_ai_trans_total_records)
    }

    private fun updateTotalRecords() {
        // Update AI Dictionary total records
        val aiDictCount = AICacheRepository.getDictionaryCacheCount()
        mTextViewAIDictTotalRecords.text = aiDictCount.toString()

        // Update AI Translator total records
        val aiTransCount = AICacheRepository.getTranslatorCacheCount()
        mTextViewAITransTotalRecords.text = aiTransCount.toString()
    }

    private fun setEventListeners() {
        // AI Dictionary section
        mSwitchCleanAllAIDict.setOnCheckedChangeListener { _, isChecked ->
            mEditTextAIDictRecords.isEnabled = !isChecked
        }

        mBtnCleanAIDict.setOnClickListener {
            handleCleanAIDictionary()
        }

        // AI Translator section
        mSwitchCleanAllAITrans.setOnCheckedChangeListener { _, isChecked ->
            mEditTextAITransRecords.isEnabled = !isChecked
        }

        mBtnCleanAITrans.setOnClickListener {
            handleCleanAITranslator()
        }
    }

    private fun handleCleanAIDictionary() {
        if (mSwitchCleanAllAIDict.isChecked) {
            // Clean all AI dictionary records
            showConfirmDialog("AI Dictionary", "all", true, 0)
        } else {
            // Clean specific number of AI dictionary records
            val inputText = mEditTextAIDictRecords.text.toString()
            if (inputText.isEmpty()) {
                Toast.makeText(this, "Please enter a number of records to clean", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                val recordCount = inputText.toInt()
                if (recordCount < 0) {
                    Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show()
                    return
                }

                showConfirmDialog("AI Dictionary", recordCount.toString(), false, recordCount)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCleanAITranslator() {
        if (mSwitchCleanAllAITrans.isChecked) {
            // Clean all AI translator records
            showConfirmDialog("AI Translator", "all", true, 0)
        } else {
            // Clean specific number of AI translator records
            val inputText = mEditTextAITransRecords.text.toString()
            if (inputText.isEmpty()) {
                Toast.makeText(this, "Please enter a number of records to clean", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                val recordCount = inputText.toInt()
                if (recordCount < 0) {
                    Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show()
                    return
                }

                showConfirmDialog("AI Translator", recordCount.toString(), false, recordCount)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showConfirmDialog(type: String, count: String, cleanAll: Boolean, recordCount: Int) {
        val message = if (cleanAll) {
            "Are you sure you want to clean ALL records from $type?"
        } else {
            "Are you sure you want to clean $count records from $type?"
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Clean")
            .setMessage(message)
            .setPositiveButton("Confirm") { _, _ ->
                if (type == "AI Dictionary") {
                    performCleanAIDictionary(cleanAll, recordCount)
                } else {
                    performCleanAITranslator(cleanAll, recordCount)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performCleanAIDictionary(cleanAll: Boolean, recordCount: Int) {
        try {
            val deletedCount = if (cleanAll) {
                AIConfigRepository.deleteAllAIDictionaryCache()
            } else {
                AIConfigRepository.deleteOldestAIDictionaryCache(recordCount)
            }
            Toast.makeText(this, "Successfully cleaned $deletedCount AI Dictionary records", Toast.LENGTH_LONG).show()
            updateTotalRecords() // Update the total records display
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to clean AI Dictionary records: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun performCleanAITranslator(cleanAll: Boolean, recordCount: Int) {
        try {
            val deletedCount = if (cleanAll) {
                AIConfigRepository.deleteAllAITranslatorCache()
            } else {
                AIConfigRepository.deleteOldestAITranslatorCache(recordCount)
            }
            Toast.makeText(this, "Successfully cleaned $deletedCount AI Translator records", Toast.LENGTH_LONG).show()
            updateTotalRecords() // Update the total records display
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to clean AI Translator records: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
