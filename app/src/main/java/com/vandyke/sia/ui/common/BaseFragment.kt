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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(hasOptionsMenu)
        return inflater.inflate(layoutResId, container, false)
    }

    open fun onBackPressed(): Boolean = false

    /** called in onResume and in onHiddenChanged when hidden is false. Basically, called whenever
     * the fragment is newly visible to the user */
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

    override fun onResume() {
        super.onResume()
        activity!!.invalidateOptionsMenu()
        onShow()
    }

    override fun onPause() {
        super.onPause()
        onHide()
    }
}