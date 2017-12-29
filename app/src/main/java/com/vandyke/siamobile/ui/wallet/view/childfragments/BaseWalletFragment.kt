/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.ui.wallet.view.childfragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.siamobile.ui.wallet.viewmodel.WalletViewModel

abstract class BaseWalletFragment : Fragment() {
    protected abstract val layout: Int
    protected lateinit var viewModel: WalletViewModel
    var height = 0

    abstract fun create(view: View, savedInstanceState: Bundle?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(layout, null)
        /* set this because the first touch listener to return true receives the rest of the touch events,
           which we want so that the SwipeableFrameLayout can intercept them and determine swipes */
        view.setOnTouchListener { v, event ->
            true
        }
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)
        height = view.measuredHeight
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(parentFragment!!).get(WalletViewModel::class.java)
        create(view, savedInstanceState)
    }
}