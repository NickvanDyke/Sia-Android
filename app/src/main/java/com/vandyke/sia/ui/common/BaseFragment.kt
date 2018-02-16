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
    open val layoutResId: Int = 0
    open val hasOptionsMenu = false
    /** this is so that if the activity is recreated, therefore recreating this fragment, it's
     *  onShow() won't be called if it wasn't visible before recreation. */
    private var wasVisible = false
    private var recreating = false

    /** need this variable because isVisible sometimes returns false in onResume if this is the first fragment being shown */
    private var firstTimeVisible = true

    val toolbar: Toolbar
        get() = (activity as MainActivity).toolbar

    val actionBar: ActionBar
        get() = (activity as AppCompatActivity).supportActionBar!!

    // could maybe use userVisibleHint to simplify the above? It persists across save states and recreations apparently

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (savedInstanceState != null) {
            recreating = true
            wasVisible = savedInstanceState["visible"] as Boolean
            firstTimeVisible = false
        }
        setHasOptionsMenu(hasOptionsMenu)
//        println("${this.javaClass.simpleName}---------------\nrecreating: $recreating\nwasVisible: $wasVisible\nfirstTimeVisible: $firstTimeVisible")
        return if (layoutResId != 0)
            inflater.inflate(layoutResId, container, false)
        else
            null
    }

    /** returns true if the back press was consumed/used by this fragment, otherwise false */
    open fun onBackPressed(): Boolean = false

    /** called in onResume and in onHiddenChanged when hidden is false. Basically, called whenever
     * the fragment is newly visible to the user. */
    open fun onShow() {}

    open fun onHide() {}

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            onShow()
            logScreen()
            activity!!.invalidateOptionsMenu()
        } else {
            onHide()
        }
    }

    /** call through to the super implementation when overriding */
    override fun onStart() {
        super.onStart()
        activity!!.invalidateOptionsMenu()
        if (recreating) {
            recreating = false
            if (wasVisible) {
                onShow()
                logScreen()
            }
        } else if (firstTimeVisible || isVisible) {
            onShow()
            logScreen()
            firstTimeVisible = false
        }
    }

    /** call through to the super implementation when overriding */
    override fun onStop() {
        super.onStop()
        if (isVisible)
            onHide()
    }

    /** call through to the super implementation when overriding */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("visible", isVisible)
    }

    private fun logScreen() {
        Analytics.setCurrentScreen(this)
    }
}