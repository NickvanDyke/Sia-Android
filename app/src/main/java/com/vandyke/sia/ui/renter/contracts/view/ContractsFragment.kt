/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.contracts.view

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.dagger.SiaViewModelFactory
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.renter.contracts.ContractsViewModel
import com.vandyke.sia.util.rx.observe
import com.vandyke.sia.util.setColors
import com.vandyke.sia.util.snackbar
import kotlinx.android.synthetic.main.fragment_contracts.*
import javax.inject.Inject

class ContractsFragment : BaseFragment() {
    override val title: String = "Contracts"
    override val layoutResId: Int = R.layout.fragment_contracts

    @Inject
    lateinit var factory: SiaViewModelFactory
    @Inject
    lateinit var siadStatus: SiadStatus

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context!!.getAppComponent().inject(this)

        val vm = ViewModelProviders.of(this, factory).get(ContractsViewModel::class.java)

        val adapter = ContractsAdapter()
        contracts_list.adapter = adapter

        contracts_list_swiperefresh.setColors(context!!)
        contracts_list_swiperefresh.setOnRefreshListener(vm::refresh)

        vm.refreshing.observe(this, contracts_list_swiperefresh::setRefreshing)

        vm.contracts.observe(this, adapter::submitList)

        vm.error.observe(this) {
            it.snackbar(view, siadStatus.state.value!!)
        }

        siadStatus.state.observe(this) {
            if (it == SiadStatus.State.SIAD_LOADED)
                vm.refresh()
        }
    }
}