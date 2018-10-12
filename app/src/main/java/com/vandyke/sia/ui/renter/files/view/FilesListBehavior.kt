/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.updatePadding
import net.cachapa.expandablelayout.ExpandableLayout

class FilesListBehavior(context: Context, attributeSet: AttributeSet) : androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior<View>(context, attributeSet) {

    override fun onDependentViewChanged(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: View, dependency: View): Boolean {
        child.updatePadding(bottom = dependency.height) // don't include dependency.translationY if I don't want it to account for snackbar moving it up too
        return true
    }

    override fun layoutDependsOn(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is ExpandableLayout
    }
}