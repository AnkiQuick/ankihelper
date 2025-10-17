package com.lmyby.ankiquicker.ui.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import com.lmyby.ankiquicker.R

/**
 * Base activity for editor screens with consistent save behavior
 * Converted to Kotlin as part of Phase 5 activity migration
 * Modernized to use OnBackPressedDispatcher in Phase 14.5
 */
abstract class BaseEditorActivity : BaseActivity() {

    private var hasUnsavedChanges = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Modern back press handling using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedChanges) {
                    showUnsavedChangesDialog()
                } else {
                    // Disable this callback and let the default behavior handle back
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun setupToolbar() {
        super.setupToolbar()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        // Editor-specific toolbar setup if needed
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                handleBackNavigation()
                true
            }
            R.id.action_save -> {
                attemptSave()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Handle back navigation with unsaved changes check
     */
    override fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog()
        } else {
            super.handleBackNavigation()
        }
    }

    /**
     * Show dialog for unsaved changes
     */
    private fun showUnsavedChangesDialog() {
        DialogHelper.showConfirmDialog(
            this,
            getString(R.string.unsaved_changes),
            getString(R.string.unsaved_changes_message)
        ) { _, _ ->
            // Discard changes and exit
            hasUnsavedChanges = false
            NavUtils.navigateUpFromSameTask(this)
        }
    }

    /**
     * Attempt to save data
     */
    private fun attemptSave() {
        if (validateInput()) {
            if (saveData()) {
                hasUnsavedChanges = false
                finish()
            }
        }
    }

    /**
     * Validate user input before saving
     */
    protected abstract fun validateInput(): Boolean

    /**
     * Save data and return success status
     */
    protected abstract fun saveData(): Boolean

    /**
     * Mark that there are unsaved changes
     */
    protected fun setHasUnsavedChanges(hasChanges: Boolean) {
        this.hasUnsavedChanges = hasChanges
    }

    /**
     * Get current unsaved changes state
     */
    protected fun hasUnsavedChanges(): Boolean {
        return hasUnsavedChanges
    }

    /**
     * Show save success message
     */
    protected open fun showSaveSuccess() {
        // Override in subclasses to show success message
    }

    /**
     * Show save error message
     */
    protected open fun showSaveError(error: String) {
        // Override in subclasses to show error message
    }
}
