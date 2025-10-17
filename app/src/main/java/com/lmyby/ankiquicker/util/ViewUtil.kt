package com.lmyby.ankiquicker.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.lmyby.ankiquicker.MyApplication
import java.util.concurrent.atomic.AtomicInteger

/**
 * Converted to Kotlin as part of Phase 1 utility migration
 * Modernized Display APIs in Phase 14.10
 */
object ViewUtil {

    const val FRAME_DURATION: Long = 1000 / 60

    private val sNextGeneratedId = AtomicInteger(1)

    @SuppressLint("NewApi")
    @JvmStatic
    fun generateViewId(): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            while (true) {
                val result = sNextGeneratedId.get()
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                var newValue = result + 1
                if (newValue > 0x00FFFFFF)
                    newValue = 1 // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue))
                    return result
            }
        } else {
            return View.generateViewId()
        }
    }

    @JvmStatic
    fun hasState(states: IntArray?, state: Int): Boolean {
        if (states == null)
            return false

        for (state1 in states)
            if (state1 == state)
                return true

        return false
    }

    @JvmStatic
    fun setBackground(v: View, drawable: Drawable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            v.background = drawable
        else
            @Suppress("DEPRECATION")
            v.setBackgroundDrawable(drawable)
    }

    @JvmStatic
    fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            MyApplication.getContext().resources.displayMetrics
        ).toInt()
    }

    @JvmStatic
    fun px2dp(px: Float): Float {
        return px / MyApplication.getContext().resources.displayMetrics.density
    }

    @JvmStatic
    fun sp2px(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            MyApplication.getContext().resources.displayMetrics
        )
    }

    @JvmStatic
    fun px2sp(px: Float): Float {
        val displayMetrics = MyApplication.getContext().resources.displayMetrics
        @Suppress("DEPRECATION")
        return px / displayMetrics.scaledDensity
    }

    @JvmStatic
    fun hideInputMethod(view: View) {
        val imm = MyApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    @JvmStatic
    fun requestInputMethodIfShow(view: EditText) {
        val imm = MyApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            view.requestFocus()
            view.setSelection(view.text.length, view.text.length)
        }
    }

    @JvmStatic
    fun showInputMethod(view: View) {
        val imm = MyApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    @JvmStatic
    fun isNavigationBarShow(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Modern API (Android 11+)
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                android.view.WindowInsets.Type.navigationBars()
            )
            insets.bottom > 0 || insets.left > 0 || insets.right > 0
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            @Suppress("DEPRECATION")
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            val realSize = Point()
            @Suppress("DEPRECATION")
            display.getSize(size)
            @Suppress("DEPRECATION")
            display.getRealSize(realSize)
            realSize.y != size.y
        } else {
            val menu = ViewConfiguration.get(activity).hasPermanentMenuKey()
            val back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            !(menu || back)
        }
    }

    @JvmStatic
    fun getNavigationBarHeight(activity: Activity): Int {
        if (!isNavigationBarShow(activity)) {
            return 0
        }
        val resources = activity.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        // 获取NavigationBar的高度
        return resources.getDimensionPixelSize(resourceId)
    }

    @JvmStatic
    fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Modern API (Android 11+)
            val windowMetrics = activity.windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            @Suppress("DEPRECATION")
            val localDisplayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            (activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(
                localDisplayMetrics
            )
            localDisplayMetrics.widthPixels
        }
    }

    @JvmStatic
    fun getSceenHeight(activity: Activity): Int {
        @Suppress("DEPRECATION")
        return activity.windowManager.defaultDisplay.height + getNavigationBarHeight(activity)
    }

    // The rest of the file contains commented-out code for applying styles
    // which has been preserved in comments but not converted
}
