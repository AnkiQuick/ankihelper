package com.lmyby.ankihelper.ui.popup

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.util.Utils

/**
 * Manages AI translation operations for PopupActivity
 * Handles async translation, UI state, and result display
 * Converted to Kotlin as part of Phase 12 popup manager conversion
 */
class PopupTranslationManager(
    private val activity: Activity,
    private val handler: Handler,
    private val btnTranslation: ImageButton,
    private val cardViewTranslation: CardView,
    private val editTextTranslation: EditText
) {

    // Callback interface for translation
    interface TranslationCallback {
        @Throws(Exception::class)
        fun performTranslation(text: String): String
    }

    private var translationCallback: TranslationCallback? = null

    val translationText: String
        get() = editTextTranslation.text.toString()

    fun setTranslationCallback(callback: TranslationCallback?) {
        this.translationCallback = callback
    }

    /**
     * Perform async translation
     */
    fun asyncTranslate(textToProcess: String?) {
        if (textToProcess.isNullOrBlank()) return

        showTranslateLoading()

        Thread {
            try {
                translationCallback?.let { callback ->
                    val result = callback.performTranslation(textToProcess)
                    val message = handler.obtainMessage()
                    message.obj = result
                    message.what = TRANSLATION_DONE
                    handler.sendMessage(message)
                }
            } catch (e: Exception) {
                val error = "AI Translation Failed: ${e.message}"
                val message = handler.obtainMessage()
                message.obj = error
                message.what = TRANSLATION_FAILED
                handler.sendMessage(message)
            }
        }.start()
    }

    /**
     * Show translation in normal state (ready)
     */
    fun showTranslateNormal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            btnTranslation.setImageResource(
                Utils.getResIdFromAttribute(activity, R.attr.icon_translate_normal)
            )
        }
    }

    /**
     * Show translation in loading state
     */
    fun showTranslateLoading() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            btnTranslation.setImageResource(
                Utils.getResIdFromAttribute(activity, R.attr.icon_translate_wait)
            )
        }
    }

    /**
     * Show translation in done state
     */
    fun showTranslateDone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            btnTranslation.setImageResource(
                Utils.getResIdFromAttribute(activity, R.attr.icon_translate_done)
            )
        }
    }

    /**
     * Show or hide translation card view
     */
    fun showTranslationCardView(show: Boolean) {
        cardViewTranslation.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * Set translation text
     */
    fun setTranslationText(text: String) {
        editTextTranslation.setText(text)
    }

    /**
     * Enable or disable translation button
     */
    fun setTranslationButtonEnabled(enabled: Boolean) {
        btnTranslation.isEnabled = enabled
    }

    companion object {
        private const val TAG = "PopupTranslationManager"

        // Message types for handler
        const val TRANSLATION_DONE = 3
        const val TRANSLATION_FAILED = 4
    }
}
