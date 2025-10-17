package com.lmyby.ankihelper.ui.popup

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import com.ichi2.anki.api.NoteInfo
import com.lmyby.ankihelper.MyApplication
import com.lmyby.ankihelper.util.Constant

/**
 * Handles intent processing for PopupActivity
 * Extracts text, notes, tags, and other data from incoming intents
 * Converted to Kotlin as part of Phase 12 popup manager conversion
 */
class PopupIntentHandler(private val activity: Activity) {

    /**
     * Result of intent processing
     */
    data class IntentData(
        var textToProcess: String = "",
        var targetWord: String? = null,
        var url: String = "",
        var noteEditedByUser: String = "",
        var tagEditedByUser: MutableSet<String> = HashSet(),
        var updateNoteId: Long = 0L,
        var updateAction: String? = null,
        var planNameFromIntent: String? = null,
        var isFromAndroidQClipboard: Boolean = false
    )

    /**
     * Process the activity intent and extract all relevant data
     */
    fun processIntent(intent: Intent?): IntentData {
        val data = IntentData()

        if (intent == null) {
            Log.d(TAG, "Intent is null")
            return data
        }

        val action = intent.action
        val type = intent.type
        Log.d(TAG, "Handle intent, action: $action, type: $type")

        if (action == null || type == null) {
            Log.d(TAG, "Action or type is null")
            return data
        }

        // Process ACTION_SEND
        if (Intent.ACTION_SEND == action && type == "text/plain") {
            processActionSend(intent, data)
        }

        // Process ACTION_PROCESS_TEXT
        if (Intent.ACTION_PROCESS_TEXT == action && type == "text/plain") {
            processActionProcessText(intent, data)
        }

        // Trim and validate text
        data.textToProcess = data.textToProcess.trim()

        if (data.textToProcess.isEmpty()) {
            Log.w(TAG, "Text to process is empty after trimming")
        } else {
            Log.i(TAG, "Successfully received text from intent action: $action, length: ${data.textToProcess.length}")
        }

        Log.d(TAG, "Final text to process length: ${data.textToProcess.length}")

        return data
    }

    /**
     * Process ACTION_SEND intent
     */
    private fun processActionSend(intent: Intent, data: IntentData) {
        Log.d(TAG, "Handling ACTION_SEND")

        val base64 = intent.getStringExtra(Constant.INTENT_ANKIHELPER_BASE64)
        data.textToProcess = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""

        Log.d(TAG, "ACTION_SEND text length: ${data.textToProcess.length}")

        // Debug log
        if (data.textToProcess.isNotEmpty()) {
            val preview = if (data.textToProcess.length > 200) {
                data.textToProcess.substring(0, 200) + "..."
            } else {
                data.textToProcess
            }
            Log.d(TAG, "ACTION_SEND text preview: ${preview.replace("\n", "\\n")}")
        }

        // Check for clipboard fallback flag
        if (data.textToProcess == Constant.USE_CLIPBOARD_CONTENT_FLAG) {
            handleClipboardFallback(data)
        }

        // Handle base64 encoding
        if (base64 != null && base64 != "0" && data.textToProcess.isNotEmpty()) {
            try {
                data.textToProcess = String(Base64.decode(data.textToProcess, Base64.DEFAULT))
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Base64 decode error", e)
            }
        }

        // Extract additional data
        data.targetWord = intent.getStringExtra(Constant.INTENT_ANKIHELPER_TARGET_WORD)
        data.url = intent.getStringExtra(Constant.INTENT_ANKIHELPER_TARGET_URL) ?: ""
        data.planNameFromIntent = intent.getStringExtra(Constant.INTENT_ANKIHELPER_PLAN_NAME)

        val noteEditedByUser = intent.getStringExtra(Constant.INTENT_ANKIHELPER_NOTE)
        if (noteEditedByUser != null) {
            data.noteEditedByUser = noteEditedByUser
        }

        val updateId = intent.getStringExtra(Constant.INTENT_ANKIHELPER_NOTE_ID)
        data.updateAction = intent.getStringExtra(Constant.INTENT_ANKIHELPER_UPDATE_ACTION)

        if (!updateId.isNullOrEmpty()) {
            try {
                data.updateNoteId = updateId.toLong()
                if (data.updateNoteId > 0) {
                    loadExistingNoteTags(data)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing note ID: ${e.message}")
                data.updateNoteId = -1L
                data.tagEditedByUser = HashSet()
            }
        }
    }

    /**
     * Process ACTION_PROCESS_TEXT intent
     */
    private fun processActionProcessText(intent: Intent, data: IntentData) {
        Log.d(TAG, "Handling ACTION_PROCESS_TEXT")

        // Try multiple methods to extract text
        data.textToProcess = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: ""

        // Fallback: try alternative extra keys
        if (data.textToProcess.isEmpty()) {
            val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
            if (text != null) {
                data.textToProcess = text.toString()
            }
        }

        // Additional fallback
        if (data.textToProcess.isEmpty()) {
            data.textToProcess = intent.getStringExtra("android.intent.extra.PROCESS_TEXT") ?: ""
        }

        Log.d(TAG, "ACTION_PROCESS_TEXT text length: ${data.textToProcess.length}")

        // Debug log
        if (data.textToProcess.isNotEmpty()) {
            val preview = if (data.textToProcess.length > 200) {
                data.textToProcess.substring(0, 200) + "..."
            } else {
                data.textToProcess
            }
            Log.d(TAG, "ACTION_PROCESS_TEXT text preview: ${preview.replace("\n", "\\n")}")
        } else {
            Log.w(TAG, "ACTION_PROCESS_TEXT received but no text content found in extras")
            // Debug available extras
            val extras = intent.extras
            if (extras != null) {
                Log.d(TAG, "Available extras: ${extras.keySet()}")
                for (key in extras.keySet()) {
                    val value = extras.get(key)
                    Log.d(TAG, "Extra $key = ${value?.toString() ?: "null"}")
                }
            }
        }
    }

    /**
     * Handle clipboard fallback for Android 10+
     */
    private fun handleClipboardFallback(data: IntentData) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, check if we have focus before accessing clipboard
            if (activity.hasWindowFocus()) {
                Log.d(TAG, "Attempting clipboard access with focus")
                data.isFromAndroidQClipboard = true
            } else {
                Log.w(TAG, "Cannot access clipboard - no focus, skipping clipboard fallback")
                data.textToProcess = ""
            }
        } else {
            // For older Android versions, proceed with clipboard access
            data.isFromAndroidQClipboard = true
        }
    }

    /**
     * Load tags from existing note for update operations
     */
    private fun loadExistingNoteTags(data: IntentData) {
        try {
            val note = MyApplication.getAnkiDroid(MyApplication.getContext())
                .api.getNote(data.updateNoteId)
            if (note != null) {
                val tagsSet = note.tags
                data.tagEditedByUser = if (tagsSet != null && tagsSet.isNotEmpty()) {
                    HashSet(tagsSet)
                } else {
                    HashSet()
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException accessing AnkiDroid API: ${e.message}")
            data.tagEditedByUser = HashSet()
        } catch (e: Exception) {
            Log.w(TAG, "Error getting note tags: ${e.message}")
            data.tagEditedByUser = HashSet()
        }
    }

    /**
     * Check clipboard permissions for Android 10+
     */
    fun checkClipboardPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (activity.checkSelfPermission("android.permission.READ_CLIPBOARD_IN_BACKGROUND")
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Clipboard background access may be restricted on Android 10+")
            }
        }
    }

    companion object {
        private const val TAG = "PopupIntentHandler"
    }
}
