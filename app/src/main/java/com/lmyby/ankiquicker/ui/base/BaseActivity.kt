package com.lmyby.ankiquicker.ui.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.Settings
import com.lmyby.ankiquicker.data.ThemeManager

/**
 * Base activity that provides consistent UI behavior across all activities
 * Converted to Kotlin as part of Phase 5 activity migration
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeIfNeeded()
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())
        setupToolbar()
        initializeViews()
        setupListeners()
    }

    /**
     * Apply theme based on user preferences
     */
    private fun applyThemeIfNeeded() {
        ThemeManager.applyTheme(this)
    }

    /**
     * Setup toolbar with consistent behavior
     */
    protected open fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = getTitle()
            }
        }
    }

    /**
     * Get layout resource ID for this activity
     */
    protected abstract fun getLayoutResId(): Int

    /**
     * Initialize views after layout is inflated
     */
    protected abstract fun initializeViews()

    /**
     * Setup event listeners for views
     */
    protected abstract fun setupListeners()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                handleBackNavigation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Handle back navigation consistently
     */
    protected open fun handleBackNavigation() {
        NavUtils.navigateUpFromSameTask(this)
    }

    /**
     * Show loading state
     */
    protected open fun showLoading() {
        // Override in subclasses to show loading indicator
    }

    /**
     * Hide loading state
     */
    protected open fun hideLoading() {
        // Override in subclasses to hide loading indicator
    }

    /**
     * Show error message
     */
    protected open fun showError(message: String) {
        // Override in subclasses to show error message
    }

    /**
     * Check if night mode is active
     */
    protected fun isNightMode(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}
