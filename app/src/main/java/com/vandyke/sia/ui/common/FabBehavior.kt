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
/* TODO: This causes the start/end of RecyclerView behavior to not work, i.e. when I fling to the top/bottom of the RV,
the colored bubble indicating that it's out of items on that end doesn't appear, and the scrollbar doesn't immediately
disappear as it should, and instead takes a couple seconds (faster fling = longer to disappear - because the RV think's
its scrolling further?). So it seems the RV thinks it is still scrolling, even past the beginning and end of items?
As you know, tapping a RV while it's scrolling will catch it and stop the scrolling. This is a problem here, because if
I fling the list to the top/bottom and then attempt to touch an item, the RV thinks it is still scrolling, and so the
item doesn't receive the touch, even though the RV isn't actually scrolling and is stopped at the top/bottom.
I'm quite sure it's because of the added functionality in the Behavior I'm using. Here's the class: https://pastebin.com/Wm82S5K9.
I did it very similarly to an implementation I found online, and didn't see the problem I'm having mentioned anywhere there.
If I always return false from onStartNestedScroll, as the default implementation of CoordinatorLayout.Behavior does, then I
don't have this problem. However obviously the functionality doesn't work then. Not overriding onNestedScroll doesn't solve
anything, so I don't think it's something there. */
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
                    .withLayer()
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
                    .withLayer()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(OvershootInterpolator())
                    .setDuration(250)
                    .withStartAction(child::visible)
        }
    }
}