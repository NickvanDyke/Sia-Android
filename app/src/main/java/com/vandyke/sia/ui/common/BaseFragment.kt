/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.sia.ui.main.MainActivity
import com.vandyke.sia.util.Analytics
import kotlinx.android.synthetic.main.activity_main.*

abstract class BaseFragment : Fragment() {
    open val layoutResId: Int = -1
    open val hasOptionsMenu = false

    val toolbar: Toolbar
        get() = (activity as MainActivity).toolbar

    val actionBar: ActionBar
        get() = (activity as AppCompatActivity).supportActionBar!!

    /** call through to the super implementation when overriding */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {setHasOptionsMenu(hasOptionsMenu)
        return if (layoutResId != -1)
            inflater.inflate(layoutResId, container, false)
        else
            null
    }

    /** returns true if the back press was consumed/used by this fragment, otherwise false */
    open fun onBackPressed(): Boolean = false

    /** called in onResume and in onHiddenChanged when hidden is false. Basically, called whenever
     * the fragment is newly visible to the user. */
    open fun onShow() {
        userVisibleHint = true
        logScreen()
    }

    open fun onHide() {
        userVisibleHint = false
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            onShow()
            activity!!.invalidateOptionsMenu()
        } else {
            onHide()
        }
    }

    /** call through to the super implementation when overriding */
    override fun onStart() {
        super.onStart()
        activity!!.invalidateOptionsMenu()
        if (userVisibleHint)
            onShow()
    }

    /** call through to the super implementation when overriding */
    override fun onStop() {
        super.onStop()
        if (userVisibleHint)
            onHide()
    }

    private fun logScreen() {
        Analytics.setCurrentScreen(this)
    }
}