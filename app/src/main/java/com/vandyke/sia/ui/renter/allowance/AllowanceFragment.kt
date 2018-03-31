/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.allowance

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.vandyke.sia.R
import com.vandyke.sia.dagger.SiaViewModelFactory
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.renter.allowance.AllowanceViewModel.Currency
import com.vandyke.sia.ui.renter.allowance.AllowanceViewModel.Metrics.*
import com.vandyke.sia.util.*
import com.vandyke.sia.util.rx.observe
import kotlinx.android.synthetic.main.fragment_allowance.*
import javax.inject.Inject

class AllowanceFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_allowance
    override val hasOptionsMenu = true
    override val title: String = "Allowance"

    @Inject
    lateinit var factory: SiaViewModelFactory
    private lateinit var vm: AllowanceViewModel

    @Inject
    lateinit var siadStatus: SiadStatus

    private var highlightedX = -1f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context!!.getAppComponent().inject(this)

        vm = ViewModelProviders.of(this, factory).get(AllowanceViewModel::class.java)

        allowance_swiperefresh.setOnRefreshListener(vm::refresh)
        allowance_swiperefresh.setColors(context!!)

        /* chart setup */
        val dataSet = PieDataSet(listOf(
                PieEntry(1f, context!!.getDrawable(R.drawable.ic_cloud_upload_white)),
                PieEntry(1f, context!!.getDrawable(R.drawable.ic_cloud_download_white)),
                PieEntry(1f, context!!.getDrawable(R.drawable.ic_storage_white)),
                PieEntry(1f, context!!.getDrawable(R.drawable.ic_file_white)),
                PieEntry(1f, context!!.getDrawable(R.drawable.ic_money_white))),
                "Spending")
        dataSet.colors = mutableListOf(context!!.getColorRes(android.R.color.holo_purple))
                .apply { addAll(ColorTemplate.MATERIAL_COLORS.asList()) }
        dataSet.sliceSpace = 1.5f

        val data = PieData(dataSet)
        data.setDrawValues(false)

        pieChart.apply {
            this.data = data
            isRotationEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = false
            // the listener is called twice when an item is touched. Not sure why
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    vm.currentMetric.value = AllowanceViewModel.Metrics.values()[h.x.toInt()]
                }

                override fun onNothingSelected() {
                    pieChart.highlightValue(highlightedX, 0)
                }
            })

            invalidate()
        }

        /* metric spinner setup */
        val metricAdapter = ArrayAdapter<String>(context, R.layout.spinner_selected_item)
        metricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        metricAdapter.addAll(AllowanceViewModel.Metrics.values().map { it.text })
        metricSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                vm.currentMetric.value = AllowanceViewModel.Metrics.values()[position]
            }
        }
        metricSpinner.adapter = metricAdapter

        /* listeners for clicky stuff in settings */
        fundsClickable.setOnClickListener {
            DialogUtil.editTextDialog(context!!,
                    "Funds",
                    "Set",
                    { vm.setAllowance(it.text.toString().toBigDecimal().toHastings()) },
                    "Cancel",
                    editTextFunc = { hint = "Amount (SC)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL })
                    .showDialogAndKeyboard()
        }

        hostsClickable.setOnClickListener {
            DialogUtil.editTextDialog(context!!,
                    "Hosts",
                    "Set",
                    { vm.setAllowance(hosts = it.text.toString().toInt()) },
                    "Cancel",
                    editTextFunc = { hint = "Hosts"; inputType = InputType.TYPE_CLASS_NUMBER })
                    .showDialogAndKeyboard()
        }

        periodClickable.setOnClickListener {
            DialogUtil.editTextDialog(context!!,
                    "Period",
                    "Set",
                    { vm.setAllowance(period = it.text.toString().toInt()) },
                    "Cancel",
                    editTextFunc = { hint = "Blocks"; inputType = InputType.TYPE_CLASS_NUMBER })
                    .showDialogAndKeyboard()
        }

        renewWindowClickable.setOnClickListener {
            DialogUtil.editTextDialog(context!!,
                    "Renew window",
                    "Set",
                    { vm.setAllowance(renewWindow = it.text.toString().toInt()) },
                    "Cancel",
                    editTextFunc = { hint = "Blocks"; inputType = InputType.TYPE_CLASS_NUMBER })
                    .showDialogAndKeyboard()
        }

        /* viewModel observation */
        vm.refreshing.observe(this, allowance_swiperefresh::setRefreshing)

        vm.allowance.observe(this) {
            // TODO: show day equivalents of block values
            fundsValue.text = it.funds.toSC().format() + " SC"
            hostsValue.text = it.hosts.format()
            periodValue.text = it.period.format() + " blocks"
            renewWindowValue.text = it.renewwindow.format() + " blocks"
        }

        vm.currentMetric.observe(this) {
            val x = it.ordinal.toFloat()
            if (dataSet.entryCount != 0 && highlightedX != x) {
                highlightedX = x
                pieChart.highlightValue(x, 0)
            }

            metricSpinner.setSelection(it.ordinal)
        }

        vm.currentMetricValues.observe(this) { (price, spent, purchasable) ->
            val currency = " " + if (vm.currency.value == Currency.SC) "SC" else Prefs.fiatCurrency
            val metric = vm.currentMetric.value

            (metric != UNSPENT).let {
                estPriceHeader.goneUnless(it)
                tvPrice.goneUnless(it)
                purchasableHeader.goneUnless(it)
                tvPurchaseable.goneUnless(it)
            }
            if (metric == UNSPENT) {
                spentHeader.text = "Remaining funds"
                tvSpent.text = spent.toSC().format() + currency
            } else {
                spentHeader.text = "Spent"
                when (metric) {
                    STORAGE -> {
                        estPriceHeader.text = "Est. price/TB/month"
                        purchasableHeader.text = "Purchasable (1 month)"
                        tvPurchaseable.text = purchasable.format() + " TB"
                    }
                    UPLOAD, DOWNLOAD -> {
                        estPriceHeader.text = "Est. price/TB"
                        purchasableHeader.text = "Purchasable"
                        tvPurchaseable.text = purchasable.format() + " TB"
                    }
                    CONTRACT -> {
                        // TODO: I think the estimated price returned for contracts is how much it'd cost
                        // to form 50. So using that, I could calc how much it'd cost for one.
                        estPriceHeader.text = "Est. price"
                        purchasableHeader.text = "Purchasable"
                        tvPurchaseable.text = purchasable.format()
                    }
                }

                tvPrice.text = price.toSC().format() + currency
                tvSpent.text = spent.toSC().format() + currency
            }
        }

        vm.spending.observe(this) { (_, upload, download, storage, contract, unspent) ->
            /* we want a minimum value so that even if the value is zero, it will still show on the chart */
            val total = (upload + download + storage + contract + unspent).toFloat()
            val minValue = if (total == 0f) 1f else total * 0.15f
            dataSet.values[0].y = upload.toFloat().coerceAtLeast(minValue)
            dataSet.values[1].y = download.toFloat().coerceAtLeast(minValue)
            dataSet.values[2].y = storage.toFloat().coerceAtLeast(minValue)
            dataSet.values[3].y = contract.toFloat().coerceAtLeast(minValue)
            dataSet.values[4].y = unspent.toFloat().coerceAtLeast(minValue)
            dataSet.notifyDataSetChanged()
            pieChart.notifyDataSetChanged()
            pieChart.invalidate()
        }

        vm.activeTasks.observe(this) {
            progress_bar.hiddenUnless(it > 0)
        }

        vm.error.observe(this) {
            it.snackbar(allowance_swiperefresh)
        }

        siadStatus.stateEvent.observe(this) {
            if (it == SiadStatus.State.SIAD_LOADED)
                vm.refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_allowance, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.currency_button -> vm.toggleDisplayedCurrency()
            else -> return false
        }
        return true
    }
}