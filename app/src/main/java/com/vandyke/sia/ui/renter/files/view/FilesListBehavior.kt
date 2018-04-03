/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import androidx.view.updatePadding
import net.cachapa.expandablelayout.ExpandableLayout

class FilesListBehavior(context: Context, attributeSet: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attributeSet) {

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        child.updatePadding(bottom = dependency.height) // don't include dependency.translationY if I don't want it to account for snackbar moving it up too
        return true
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is ExpandableLayout
    }
}