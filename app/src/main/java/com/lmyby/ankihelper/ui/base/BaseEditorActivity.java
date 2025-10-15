package com.lmyby.ankihelper.ui.base;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import com.lmyby.ankihelper.R;

/**
 * Base activity for editor screens with consistent save behavior
 */
public abstract class BaseEditorActivity extends BaseActivity {

    private boolean hasUnsavedChanges = false;

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            // Editor-specific toolbar setup if needed
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            handleBackNavigation();
            return true;
        } else if (itemId == R.id.action_save) {
            attemptSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle back navigation with unsaved changes check
     */
    @Override
    protected void handleBackNavigation() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog();
        } else {
            super.handleBackNavigation();
        }
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Show dialog for unsaved changes
     */
    private void showUnsavedChangesDialog() {
        DialogHelper.showConfirmDialog(
                this,
                getString(R.string.unsaved_changes),
                getString(R.string.unsaved_changes_message),
                (dialog, which) -> {
                    // Discard changes and exit
                    hasUnsavedChanges = false;
                    NavUtils.navigateUpFromSameTask(this);
                }
        );
    }

    /**
     * Attempt to save data
     */
    private void attemptSave() {
        if (validateInput()) {
            if (saveData()) {
                hasUnsavedChanges = false;
                finish();
            }
        }
    }

    /**
     * Validate user input before saving
     */
    protected abstract boolean validateInput();

    /**
     * Save data and return success status
     */
    protected abstract boolean saveData();

    /**
     * Mark that there are unsaved changes
     */
    protected void setHasUnsavedChanges(boolean hasChanges) {
        this.hasUnsavedChanges = hasChanges;
    }

    /**
     * Get current unsaved changes state
     */
    protected boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /**
     * Show save success message
     */
    protected void showSaveSuccess() {
        // Override in subclasses to show success message
    }

    /**
     * Show save error message
     */
    protected void showSaveError(String error) {
        // Override in subclasses to show error message
    }
}