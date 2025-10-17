package com.lmyby.ankiquicker.ui.popup

import android.app.Activity
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.dict.Definition
import com.lmyby.ankiquicker.data.dict.IDictionary
import com.lmyby.ankiquicker.data.history.HistoryUtil
import com.lmyby.ankiquicker.data.plan.OutputPlanPOJO

/**
 * Manages dictionary search operations for PopupActivity
 * Handles async search, definition processing, and UI state
 * Converted to Kotlin as part of Phase 12 popup manager conversion
 */
class PopupSearchManager(
    private val activity: Activity,
    private val handler: Handler,
    private val progressBar: ProgressBar,
    private val btnSearch: ImageButton,
    private val btnPronounce: ImageButton,
    private val viewDefinitionList: LinearLayout
) {

    // Callback interface for definition processing
    interface DefinitionProcessor {
        fun getCardFromDefinition(def: Definition): View
    }

    private var definitionProcessor: DefinitionProcessor? = null

    fun setDefinitionProcessor(processor: DefinitionProcessor?) {
        this.definitionProcessor = processor
    }

    /**
     * Perform async dictionary search
     */
    fun asyncSearch(
        word: String,
        textToProcess: String,
        currentDictionary: IDictionary?,
        currentOutputPlan: OutputPlanPOJO?
    ) {
        if (word.isEmpty()) {
            showPronounce(false)
            return
        }
        if (currentDictionary == null || currentOutputPlan == null) {
            return
        }

        showProgressBar()
        progressBar.invalidate()
        showPronounce(true)

        Thread {
            try {
                Log.d("clicked", "yes")
                val d = currentDictionary.wordLookup(word)
                val message = handler.obtainMessage()
                message.obj = d
                message.what = PROCESS_DEFINITION_LIST
                handler.sendMessage(message)
            } catch (e: Exception) {
                val error = e.message
                val message = handler.obtainMessage()
                message.obj = error
                message.what = ASYNC_SEARCH_FAILED
                handler.sendMessage(message)
            }
        }.start()

        // Save history
        HistoryUtil.saveWordlookup(textToProcess, word)
    }

    /**
     * Process and display definition list
     */
    fun processDefinitionList(definitionList: List<Definition>) {
        if (definitionList.isEmpty()) {
            Toast.makeText(activity, R.string.definition_not_found, Toast.LENGTH_SHORT).show()
        } else {
            viewDefinitionList.removeAllViewsInLayout()
            for (def in definitionList) {
                definitionProcessor?.let { processor ->
                    val card = processor.getCardFromDefinition(def)
                    viewDefinitionList.addView(card)
                }
            }
        }
    }

    /**
     * Show progress bar, hide search button
     */
    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        btnSearch.visibility = View.GONE
    }

    /**
     * Show search button, hide progress bar
     */
    fun showSearchButton() {
        progressBar.visibility = View.GONE
        btnSearch.visibility = View.VISIBLE
    }

    /**
     * Show/hide pronunciation button
     */
    fun showPronounce(shouldShow: Boolean) {
        btnPronounce.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "PopupSearchManager"

        // Message types for handler
        const val PROCESS_DEFINITION_LIST = 1
        const val ASYNC_SEARCH_FAILED = 2
    }
}
