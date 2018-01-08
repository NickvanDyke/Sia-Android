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
     *  onShow() won't be called if it wasn't visible before recreating. I might need to do a similar
     *  thing so that onHide() isn't called as a result of recreating when it's destroyed. */
    private var visibleWhenRecreated = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (savedInstanceState != null)
            visibleWhenRecreated = savedInstanceState["visible"] as Boolean
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
        if (visibleWhenRecreated)
            onShow()
        else
            visibleWhenRecreated = true
    }

    /** call through to the super implementation when overriding */
    override fun onPause() {
        super.onPause()
        onHide()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("visible", isVisible)
    }
}