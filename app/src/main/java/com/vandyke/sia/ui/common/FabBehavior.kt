/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar.SnackbarLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.view.isVisible
import com.vandyke.sia.util.invisible
import com.vandyke.sia.util.visible

/** Adjusts a FABs height in accordance with Snackbars, and scales it in/out when a nested list is scrolled up/down */
// TODO: When I have this on a FAB (both wallet and files), any scrolling that goes all the way to the bottom causes the next
// click on a list item to not register, unless the list is scrolled up first.
// Scrolling down when already at the bottom also causes it.
class FabBehavior(context: Context, attributeSet: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attributeSet) {

    private var animatingOut = false

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        child.translationY = Math.min(0f, dependency.translationY - dependency.height)
        return true
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        child.translationY = 0f
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is SnackbarLayout
    }

    // TODO: it's something to do with this part. Just returning super (which is always false) fixes it.
    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    // commenting this out does not fix it
    override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            type: Int
    ) {
        /* scrolled down and FAB is visible, so hide it */
        if (dyConsumed > 20 && child.isVisible && !animatingOut) {
            child.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setInterpolator(AccelerateInterpolator())
                    .setDuration(175)
                    .withStartAction { animatingOut = true }
                    .withEndAction {
                        animatingOut = false
                        child.invisible()
                    }
        } else if (dyConsumed < -20 && !child.isVisible) {
            child.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(OvershootInterpolator())
                    .setDuration(250)
                    .withStartAction(child::visible)
        }
    }
}