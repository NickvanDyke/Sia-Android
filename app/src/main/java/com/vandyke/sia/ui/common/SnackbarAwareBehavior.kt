/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewPropertyAnimator
import com.google.android.material.snackbar.Snackbar.SnackbarLayout


class SnackbarAwareBehavior(context: Context, attributeSet: AttributeSet) : androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior<View>(context, attributeSet) {

    private var animation: ViewPropertyAnimator? = null

    override fun onDependentViewChanged(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: View, dependency: View): Boolean {
        animation?.cancel()
        child.translationY = Math.min(0f, dependency.translationY - dependency.height)
        return true
    }

    override fun onDependentViewRemoved(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: View, dependency: View) {
        animation = child.animate()
                .translationY(0f)
                .setDuration(325)
    }

    override fun layoutDependsOn(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is SnackbarLayout
    }
}