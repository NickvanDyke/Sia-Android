/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.wallet.view.childfragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vandyke.sia.ui.wallet.viewmodel.WalletViewModel

abstract class BaseWalletFragment : Fragment() {
    protected abstract val layout: Int
    protected lateinit var vm: WalletViewModel

    open fun create(view: View, savedInstanceState: Bundle?) {}

    /** Returns true if the fragment consumed the press. Otherwise false, so the WalletFragment can handle it */
    open fun onCheckPressed(): Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(layout, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = ViewModelProviders.of(parentFragment!!).get(WalletViewModel::class.java)
        create(view, savedInstanceState)
    }
}