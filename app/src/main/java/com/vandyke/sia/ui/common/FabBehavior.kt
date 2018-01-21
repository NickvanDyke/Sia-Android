/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar.SnackbarLayout
import android.util.AttributeSet
import android.view.View
import com.github.clans.fab.FloatingActionMenu


class FabBehavior(context: Context, attributeSet: AttributeSet) : CoordinatorLayout.Behavior<FloatingActionMenu>(context, attributeSet) {

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View): Boolean {
        val translationY = Math.min(0f, dependency.translationY - dependency.height)
        child.translationY = translationY
        return true
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View): Boolean {
        return dependency is SnackbarLayout
    }
}