/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.View
import com.github.clans.fab.FloatingActionMenu
import net.cachapa.expandablelayout.ExpandableLayout

class FilesFabBehavior(context: Context, attributeSet: AttributeSet) : CoordinatorLayout.Behavior<FloatingActionMenu>(context, attributeSet) {
    private var snackbarHeight = 0
    private var snackbarTranslationY = 0f
    private var expandableHeight = 0
    private var expandableTranslationY = 0f

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View): Boolean {
        if (dependency is ExpandableLayout) {
            expandableHeight = dependency.height
            expandableTranslationY = dependency.translationY
        } else if (dependency is Snackbar.SnackbarLayout) {
            snackbarHeight = dependency.height
            snackbarTranslationY = dependency.translationY
        }

        if (expandableHeight > 0) {
            child.translationY = Math.min(0f, expandableTranslationY - expandableHeight)
        } else {
            child.translationY = Math.min(0f, snackbarTranslationY - snackbarHeight)
        }
        return true
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View): Boolean {
        return dependency is ExpandableLayout || dependency is Snackbar.SnackbarLayout
    }
}