/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.allowance

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.appComponent
import com.vandyke.sia.dagger.SiaViewModelFactory
import com.vandyke.sia.data.siad.SiadSource
import com.vandyke.sia.ui.common.BaseFragment
import javax.inject.Inject


class RenterAllowanceFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_renter_allowance
    override val hasOptionsMenu = true

    @Inject
    lateinit var factory: SiaViewModelFactory
    private lateinit var viewModel: RenterAllowanceViewModel

    @Inject
    lateinit var siadSource: SiadSource

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appComponent.inject(this)

        viewModel = ViewModelProviders.of(this, factory).get(RenterAllowanceViewModel::class.java)


        siadSource.isSiadLoaded.observe(this) {
            if (it)
                viewModel.refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

    }
}