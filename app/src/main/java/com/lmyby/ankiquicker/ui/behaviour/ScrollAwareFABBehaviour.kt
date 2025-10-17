package com.lmyby.ankiquicker.ui.behaviour

import android.animation.Animator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lmyby.ankiquicker.R

/**
 * FloatingActionButton behavior that hides/shows the FAB based on scroll direction
 * Converted to Kotlin as part of Phase 11 utility migration
 * Modernized nested scroll API in Phase 14.8
 */
class ScrollAwareFABBehaviour(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior() {

    private var mIsAnimatingOut = false

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        // Ensure we react to vertical scrolling
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
        Log.d("scrollingfab", "$dyConsumed: $dyUnconsumed")
        if ((dyConsumed > 0 || dyUnconsumed < 0) && !mIsAnimatingOut && child.visibility == View.VISIBLE) {
            // User scrolled down and the FAB is currently visible -> hide the FAB
            animateOut(child)
        } else if ((dyConsumed < 0 || dyUnconsumed > 0) && child.visibility != View.VISIBLE) {
            // User scrolled up and the FAB is currently not visible -> show the FAB
            animateIn(child)
        }
    }

    // Same animation that FloatingActionButton.Behavior uses to hide the FAB when the AppBarLayout exits
    private fun animateOut(button: FloatingActionButton) {
        // Use modern View.animate() API (available since API 12)
        button.animate()
            .scaleX(0.0f)
            .scaleY(0.0f)
            .alpha(0.0f)
            .setInterpolator(INTERPOLATOR)
            .withLayer()
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    mIsAnimatingOut = true
                }

                override fun onAnimationCancel(animation: Animator) {
                    mIsAnimatingOut = false
                }

                override fun onAnimationEnd(animation: Animator) {
                    mIsAnimatingOut = false
                    button.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
            .start()
    }

    // Same animation that FloatingActionButton.Behavior uses to show the FAB when the AppBarLayout enters
    private fun animateIn(button: FloatingActionButton) {
        button.visibility = View.VISIBLE
        // Use modern View.animate() API (available since API 12)
        button.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .alpha(1.0f)
            .setInterpolator(INTERPOLATOR)
            .withLayer()
            .setListener(null)
            .start()
    }

    companion object {
        private val INTERPOLATOR: Interpolator = FastOutSlowInInterpolator()
    }
}
