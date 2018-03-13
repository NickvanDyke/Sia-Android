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
    abstract val title: String

    val toolbar: Toolbar
        get() = (activity as MainActivity).toolbar

    val actionBar: ActionBar
        get() = (activity as AppCompatActivity).supportActionBar!!

    val mainActivity
        get() = activity as MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(hasOptionsMenu)
        return if (layoutResId != -1)
            inflater.inflate(layoutResId, container, false)
        else
            null
    }

    /** returns true if the back press was consumed/used by this fragment, otherwise false */
    open fun onBackPressed(): Boolean = false

    private fun onShowHelper() {
        userVisibleHint = true
        logScreen()
        onShow()
    }

    private fun onHideHelper() {
        userVisibleHint = false
        onHide()
    }

    open fun onShow() {
    }

    open fun onHide() {
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            onShowHelper()
            activity!!.invalidateOptionsMenu()
        } else {
            onHideHelper()
        }
    }

    override fun onStart() {
        super.onStart()
        activity!!.invalidateOptionsMenu()
        if (userVisibleHint || isVisible)
            onShowHelper()
    }

    override fun onStop() {
        super.onStop()
        if (userVisibleHint)
            onHideHelper()
    }

    private fun logScreen() {
        Analytics.setCurrentScreen(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {

    }
}