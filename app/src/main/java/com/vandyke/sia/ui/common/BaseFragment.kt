/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BaseFragment : Fragment() {
    abstract val layoutResId: Int
    open val hasOptionsMenu = false
    /** this is so that if the activity is recreated, therefore recreating this fragment, it's
     *  onShow() won't be called if it wasn't visible before recreating. */
    private var wasVisible = false
    private var recreating = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (savedInstanceState != null) {
            recreating = true
            wasVisible = savedInstanceState["visible"] as Boolean
        }
        setHasOptionsMenu(hasOptionsMenu)
        return inflater.inflate(layoutResId, container, false)
    }

    open fun onBackPressed(): Boolean = false

    /** called in onResume and in onHiddenChanged when hidden is false. Basically, called whenever
     * the fragment is newly visible to the user. */
    open fun onShow() {}

    open fun onHide() {}

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            onShow()
            activity!!.invalidateOptionsMenu()
        } else {
            onHide()
        }
    }

    /** call through to the super implementation when overriding */
    override fun onResume() {
        super.onResume()
        activity!!.invalidateOptionsMenu()
        if (recreating) {
            recreating = false
            if (wasVisible)
                onShow()
        } else {
            onShow()
        }
    }

    /** call through to the super implementation when overriding */
    override fun onPause() {
        super.onPause()
        if (isVisible)
            onHide()
    }

    /** call through to the super implementation when overriding */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("visible", isVisible)
    }
}