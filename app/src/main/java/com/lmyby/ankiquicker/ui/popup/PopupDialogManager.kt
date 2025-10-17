package com.lmyby.ankiquicker.ui.popup

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.Settings
import com.lmyby.ankiquicker.data.database.AppDatabase
import com.lmyby.ankiquicker.data.model.UserTagRepository
import com.lmyby.ankiquicker.data.model.UserTagRepositoryHelper
import com.lmyby.ankiquicker.util.Utils

/**
 * Manages dialog operations for PopupActivity
 * Handles note and tag editing dialogs
 * Converted to Kotlin as part of Phase 12 popup manager conversion
 */
class PopupDialogManager(
    private val activity: Activity,
    private val settings: Settings
) {

    // Callback interfaces for dialog results
    interface NoteEditCallback {
        fun getCurrentNote(): String
        fun onNoteEdited(newNote: String)
    }

    interface TagEditCallback {
        fun getCurrentTags(): Set<String>
        fun onTagsEdited(newTags: Set<String>)
    }

    private var noteEditCallback: NoteEditCallback? = null
    private var tagEditCallback: TagEditCallback? = null

    fun setNoteEditCallback(callback: NoteEditCallback?) {
        this.noteEditCallback = callback
    }

    fun setTagEditCallback(callback: TagEditCallback?) {
        this.tagEditCallback = callback
    }

    /**
     * Show note editing dialog
     */
    fun showEditNoteDialog() {
        val callback = noteEditCallback ?: return

        val dialogBuilder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_note, null)
        dialogBuilder.setView(dialogView)

        val edt = dialogView.findViewById<EditText>(R.id.edit_note)
        edt.setHorizontallyScrolling(false)
        edt.maxLines = 4

        val currentNote = callback.getCurrentNote()
        edt.setText(currentNote)
        edt.setSelection(currentNote.length)

        dialogBuilder.setTitle(R.string.dialog_note)
        dialogBuilder.setPositiveButton(R.string.dialog_ok) { _, _ ->
            callback.onNoteEdited(edt.text.toString())
        }

        dialogBuilder.create().show()
    }

    /**
     * Show tag editing dialog
     */
    fun showTagDialog() {
        val callback = tagEditCallback ?: return

        val dialogBuilder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_tag, null)
        dialogBuilder.setView(dialogView)

        val editTag = dialogView.findViewById<AutoCompleteTextView>(R.id.edit_tag)
        val checkBoxSetAsDefaultTag = dialogView.findViewById<CheckBox>(R.id.checkbox_as_default_tag)
        val tagChipGroup = dialogView.findViewById<ChipGroup>(R.id.tag_chip_list)

        editTag.imeOptions = EditorInfo.IME_ACTION_DONE

        val currentTags = callback.getCurrentTags().toMutableSet()
        if (currentTags.isNotEmpty()) {
            val text = Utils.fromTagSetToString(currentTags)
            editTag.setText(text)
            editTag.setSelection(text.length)
        }
        tagChipGroup.isSingleSelection = false

        // Load tags using UserTagRepository
        val database = AppDatabase.getInstance(activity.applicationContext)
        val userTagRepository = UserTagRepository(database.userTagDao())
        val tagHelper = UserTagRepositoryHelper(userTagRepository)

        val userTagStrings: List<String>
        try {
            userTagStrings = tagHelper.getAllTagStringsBlocking()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading tags", e)
            Toast.makeText(activity, "Error loading tags", Toast.LENGTH_SHORT).show()
            return
        }

        for (tagString in userTagStrings) {
            val chip = inflater.inflate(R.layout.tag_chip_item, null) as Chip
            chip.text = tagString
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currentTags.add(chip.text.toString())
                } else {
                    currentTags.remove(chip.text.toString())
                }
                // tag1,tag2,tag3
                val text = Utils.fromTagSetToString(currentTags)
                editTag.setText(text)
                editTag.setSelection(text.length)
            }
            if (currentTags.contains(chip.text.toString())) {
                chip.isChecked = true
            }
            tagChipGroup.addView(chip)
        }

        val setDefaultQ = settings.getSetAsDefaultTag()
        checkBoxSetAsDefaultTag.isChecked = setDefaultQ
        dialogBuilder.setTitle(R.string.dialog_tag)

        dialogBuilder.setPositiveButton(R.string.dialog_ok) { _, _ ->
            val tag = editTag.text.toString().trim()
            if (tag.isEmpty()) {
                if (checkBoxSetAsDefaultTag.isChecked) {
                    currentTags.clear()
                    Toast.makeText(activity, R.string.tag_cant_be_blank, Toast.LENGTH_LONG).show()
                } else {
                    settings.setSetAsDefaultTag(false)
                    currentTags.clear()
                }
                callback.onTagsEdited(currentTags)
                return@setPositiveButton
            } else {
                val newTags = Utils.fromStringToTagSet(editTag.text.toString())
                settings.setSetAsDefaultTag(checkBoxSetAsDefaultTag.isChecked)
                settings.setDefaultTag(editTag.text.toString())

                // Save new tags using UserTagRepository
                val db = AppDatabase.getInstance(activity.applicationContext)
                val repo = UserTagRepository(db.userTagDao())
                val helper = UserTagRepositoryHelper(repo)

                for (t in newTags) {
                    if (!userTagStrings.contains(t)) { // add new tag
                        helper.insertTagString(t)
                    }
                }

                callback.onTagsEdited(newTags)
            }
        }

        dialogBuilder.create().show()
    }

    companion object {
        private const val TAG = "PopupDialogManager"
    }
}
