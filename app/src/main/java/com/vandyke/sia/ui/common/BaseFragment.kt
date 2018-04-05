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
import com.vandyke.sia.util.gone
import kotlinx.android.synthetic.main.activity_main.*
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

abstract class BaseFragment : Fragment() {
    open val layoutResId: Int = -1
    open val hasOptionsMenu = false
    abstract val title: String

    val mainActivity: MainActivity
        get() = activity as MainActivity

    val toolbar: Toolbar
        get() = mainActivity.toolbar

    val actionBar: ActionBar
        get() = (activity as AppCompatActivity).supportActionBar!!

    val progressBar: MaterialProgressBar
        get() = mainActivity.toolbar_progress_bar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(hasOptionsMenu)
        return if (layoutResId != -1)
            inflater.inflate(layoutResId, container, false)
        else
            null
    }

    /** returns true if the back press was consumed/used by this fragment, otherwise false */
    open fun onBackPressed(): Boolean = false

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
//        println("${this.javaClass.simpleName} onHiddenChanged. hidden: $hidden")
        if (!hidden) {
            onShowInternal()
            activity!!.invalidateOptionsMenu()
        } else {
            onHideInternal()
        }
    }

    override fun onStart() {
//        println("${this.javaClass.simpleName} onStart. userVisibleHint: $userVisibleHint; isVisible: $isVisible")
        super.onStart()
        activity!!.invalidateOptionsMenu()
        if (userVisibleHint || isVisible)
            onShowInternal()
    }

    override fun onStop() {
//        println("${this.javaClass.simpleName} onStop. userVisibleHint: $userVisibleHint; isVisible: $isVisible")
        super.onStop()
        if (userVisibleHint)
            onHideInternal()
    }

    // am very rarely having onShow called when it shouldn't be, by fragments in the background... not sure why
    private fun onShowInternal() {
//        println("${this.javaClass.simpleName} onShowHelper")
        userVisibleHint = true
        logScreen()
        onShow()
    }

    private fun onHideInternal() {
        progressBar.gone()
//        println("${this.javaClass.simpleName} onHideHelper")
        userVisibleHint = false
        onHide()
    }

    open fun onShow() {
    }

    open fun onHide() {
    }

    private fun logScreen() {
        Analytics.setCurrentScreen(this)
    }
}