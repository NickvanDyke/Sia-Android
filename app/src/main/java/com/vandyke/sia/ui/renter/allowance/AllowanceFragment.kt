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
import kotlinx.android.synthetic.main.allowance_setting.view.*
import kotlinx.android.synthetic.main.fragment_allowance.*
import java.math.BigDecimal
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

        /* set text on included layouts */
        funds.setting_header.text = "Funds"
        funds.setting_description.text = "The amount the renter can spend in the given period. Spent on contracts, storage, and bandwidth."

        hosts.setting_header.text = "Hosts"
        hosts.setting_description.text = "The number of hosts to form contracts with and use for storage."
        hosts.secondary_value.gone()

        period.setting_header.text = "Period"
        period.setting_description.text = "The duration of contracts formed."

        renew_window.setting_header.text = "Renew window"
        renew_window.setting_description.text = "The length before the end of a contract that it will be automatically renewed. The renter must be running, and wallet unlocked, to do so."

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

        pie_chart.apply {
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
                    pie_chart.highlightValue(highlightedX, 0)
                }
            })

            invalidate()
        }

        /* metric spinner setup */
        val metricAdapter = ArrayAdapter<String>(context, R.layout.spinner_selected_item)
        metricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        metricAdapter.addAll(AllowanceViewModel.Metrics.values().map { it.text })
        metric_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                vm.currentMetric.value = AllowanceViewModel.Metrics.values()[position]
            }
        }
        metric_spinner.adapter = metricAdapter

        /* listeners for clicky stuff in settings */
        funds.setOnClickListener {
            DialogUtil.editTextDialog(context!!,
                    "Funds",
                    "Set",
                    { vm.setAllowance(it.toBigDecimal().toHastings()) },
                    "Cancel",
                    editTextFunc = { hint = "Amount (SC)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL })
                    .showDialogAndKeyboard()
        }

        hosts.setOnClickListener {
            DialogUtil.editTextDialog(context!!,
                    "Hosts",
                    "Set",
                    { vm.setAllowance(hosts = it.toInt()) },
                    "Cancel",
                    editTextFunc = { hint = "Hosts"; inputType = InputType.TYPE_CLASS_NUMBER })
                    .showDialogAndKeyboard()
        }

        period.setOnClickListener {
            DialogUtil.editTextDialog(context!!,
                    "Period",
                    "Set",
                    { vm.setAllowance(period = it.toInt()) },
                    "Cancel",
                    editTextFunc = { hint = "Blocks"; inputType = InputType.TYPE_CLASS_NUMBER })
                    .showDialogAndKeyboard()
        }

        renew_window.setOnClickListener {
            DialogUtil.editTextDialog(context!!,
                    "Renew window",
                    "Set",
                    { vm.setAllowance(renewWindow = it.toInt()) },
                    "Cancel",
                    editTextFunc = { hint = "Blocks"; inputType = InputType.TYPE_CLASS_NUMBER })
                    .showDialogAndKeyboard()
        }

        /* viewModel observation */
        vm.refreshing.observe(this, allowance_swiperefresh::setRefreshing)

        vm.allowance.observe(this) {
            funds.primary_value.text = it.funds.toSC().format() + " SC"
            hosts.primary_value.text = it.hosts.format()
            period.primary_value.text = it.period.format() + " blocks"
            renew_window.primary_value.text = it.renewwindow.format() + " blocks"

            updateFundsFiatValue()
            period.secondary_value.text = "(~${SiaUtil.blocksToDays(it.period).format()} days)"
            renew_window.secondary_value.text = "(~${SiaUtil.blocksToDays(it.renewwindow).format()} days)"
        }

        vm.scValue.observe(this) {
            updateFundsFiatValue()
        }

        vm.remainingPeriod.observe(this) {
            // TODO: improve this. Should have header text on left, and value text on right I think
            current_period_header.text = "Current period - $it blocks remaining"
        }

        vm.currentMetric.observe(this) {
            val x = it.ordinal.toFloat()
            if (dataSet.entryCount != 0 && highlightedX != x) {
                highlightedX = x
                pie_chart.highlightValue(x, 0)
            }

            metric_spinner.setSelection(it.ordinal)
        }

        vm.currentMetricValues.observe(this) { (price, spent, purchasable) ->
            val currency = " " + if (vm.currency.value == Currency.SC) "SC" else Prefs.fiatCurrency
            val metric = vm.currentMetric.value

            (metric != UNSPENT).let {
                est_price_header.goneUnless(it)
                est_price.goneUnless(it)
                purchasable_header.goneUnless(it)
                purchasable_value.goneUnless(it)
            }
            if (metric == UNSPENT) {
                spent_header.text = "Remaining funds"
                spent_value.text = spent.toSC().format() + currency
            } else {
                spent_header.text = "Spent"
                when (metric) {
                    STORAGE -> {
                        est_price_header.text = "Est. price/TB/month"
                        purchasable_header.text = "Purchasable (1 month)"
                        purchasable_value.text = purchasable.format() + " TB"
                    }
                    UPLOAD, DOWNLOAD -> {
                        est_price_header.text = "Est. price/TB"
                        purchasable_header.text = "Purchasable"
                        purchasable_value.text = purchasable.format() + " TB"
                    }
                    CONTRACT -> {
                        est_price_header.text = "Est. price"
                        purchasable_header.text = "Purchasable"
                        purchasable_value.text = (purchasable * BigDecimal("50")).format()
                    }
                }

                est_price.text = price.toSC().format() + currency
                spent_value.text = spent.toSC().format() + currency
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
            pie_chart.notifyDataSetChanged()
            pie_chart.invalidate()
        }

        vm.activeTasks.observe(this) {
            progressBar.goneUnless(it > 0)
        }

        vm.error.observe(this) {
            it.snackbar(allowance_swiperefresh, siadStatus.state.value!!)
        }

        siadStatus.state.observe(this) {
            if (it == SiadStatus.State.SIAD_LOADED)
                vm.refresh()
        }
    }

    private fun updateFundsFiatValue() {
        val scValue = vm.scValue.value ?: return
        val fundsSc = vm.allowance.value?.funds ?: return
        funds.secondary_value.text = "(${(scValue[Prefs.fiatCurrency] * fundsSc.toSC()).format()} ${Prefs.fiatCurrency})"
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

    override fun onShow() {
        progressBar.goneUnless(vm.activeTasks.value > 0)
    }
}