package com.mmjang.ankihelper.ui.base;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.Settings;
import com.mmjang.ankihelper.data.ThemeManager;

/**
 * Base activity that provides consistent UI behavior across all activities
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyThemeIfNeeded();
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        setupToolbar();
        initializeViews();
        setupListeners();
    }

    /**
     * Apply theme based on user preferences
     */
    private void applyThemeIfNeeded() {
        ThemeManager.applyTheme(this);
    }

    /**
     * Setup toolbar with consistent behavior
     */
    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(getTitle());
            }
        }
    }

    /**
     * Get layout resource ID for this activity
     */
    protected abstract int getLayoutResId();

    /**
     * Initialize views after layout is inflated
     */
    protected abstract void initializeViews();

    /**
     * Setup event listeners for views
     */
    protected abstract void setupListeners();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            handleBackNavigation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle back navigation consistently
     */
    protected void handleBackNavigation() {
        NavUtils.navigateUpFromSameTask(this);
    }

    /**
     * Show loading state
     */
    protected void showLoading() {
        // Override in subclasses to show loading indicator
    }

    /**
     * Hide loading state
     */
    protected void hideLoading() {
        // Override in subclasses to hide loading indicator
    }

    /**
     * Show error message
     */
    protected void showError(String message) {
        // Override in subclasses to show error message
    }

    /**
     * Check if night mode is active
     */
    protected boolean isNightMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}