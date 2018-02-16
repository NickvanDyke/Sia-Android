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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.vandyke.sia.R
import com.vandyke.sia.appComponent
import com.vandyke.sia.dagger.SiaViewModelFactory
import com.vandyke.sia.data.siad.SiadSource
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

    @Inject
    lateinit var factory: SiaViewModelFactory
    private lateinit var vm: AllowanceViewModel

    @Inject
    lateinit var siadSource: SiadSource

    private var currencyButton: MenuItem? = null

    private var highlightedX = -1f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appComponent.inject(this)

        vm = ViewModelProviders.of(this, factory).get(AllowanceViewModel::class.java)

        allowanceSwipe.setOnRefreshListener { vm.refresh() }
        allowanceSwipe.setColors(context!!)

        /* chart setup */
        val dataSet = PieDataSet(listOf(
                PieEntry(0f, context!!.getDrawable(R.drawable.ic_cloud_upload)),
                PieEntry(0f, context!!.getDrawable(R.drawable.ic_cloud_download)),
                PieEntry(0f, context!!.getDrawable(R.drawable.ic_storage)),
                PieEntry(0f, context!!.getDrawable(R.drawable.ic_file)),
                PieEntry(0f, context!!.getDrawable(R.drawable.ic_money))),
                "Spending")
        val colors = ColorTemplate.MATERIAL_COLORS.toMutableList()
        colors.add(0, context!!.getColorRes(android.R.color.holo_purple))
        dataSet.colors = colors
        dataSet.sliceSpace = 2f

        val data = PieData(dataSet)
        data.setDrawValues(false)

        with(pieChart) {
            this.data = data
            description.isEnabled = false
            legend.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = false
            // the listener is called twice when an item is touched. Not sure why
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    vm.currentMetric.value = when (h.x) {
                        0f -> UPLOAD
                        1f -> DOWNLOAD
                        2f -> STORAGE
                        3f -> CONTRACT
                        4f -> UNSPENT
                        else -> throw IllegalArgumentException("Invalid x value: ${h.x}")
                    }
                }

                override fun onNothingSelected() {
                    pieChart.highlightValue(highlightedX, 0)
                }
            })

            invalidate() // TODO: pie chart is invisible until after recreation, or unless
            // displayFragment(AllowanceFragment::class.java) is called at the end of MainActivity onCreate.
            // why??? hopefully will discover while working on other stuff here
        }

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
        vm.refreshing.observe(this) {
            allowanceSwipe.isRefreshing = it
        }

        vm.allowance.observe(this) {
            with(it) {
                fundsValue.text = funds.toSC().format() + " SC"
                hostsValue.text = hosts.format()
                periodValue.text = period.format() + " blocks"
                renewWindowValue.text = renewwindow.format() + " blocks"
            }
        }

        vm.currentMetric.observe(this) {
            val x = it.ordinal.toFloat()
            if (dataSet.entryCount != 0 && highlightedX != x) {
                highlightedX = x
                pieChart.highlightValue(x, 0)
            }

            metricText.text = it.text
        }

        vm.currentMetricValues.observe(this) { (price, spent, purchasable) ->
            val currency = " " + vm.currency.value.text
            val metric = vm.currentMetric.value
            
            if (metric == UNSPENT) {
                spentHeader.text = "Remaining funds"
                tvSpent.text = spent.toSC().format() + currency
                estPriceHeader.visibility = View.INVISIBLE
                tvPrice.visibility = View.INVISIBLE
                purchasableHeader.visibility = View.INVISIBLE
                tvPurchaseable.visibility = View.INVISIBLE
            } else {
                spentHeader.text = "Spent"
                estPriceHeader.visibility = View.VISIBLE
                tvPrice.visibility = View.VISIBLE
                purchasableHeader.visibility = View.VISIBLE
                tvPurchaseable.visibility = View.VISIBLE
                if (metric == STORAGE) {
                    estPriceHeader.text = "Est. price/TB/month"
                    purchasableHeader.text = "Purchasable (1 month)"
                    tvPurchaseable.text = purchasable.format() + " TB"
                } else if (metric == UPLOAD || metric == DOWNLOAD) {
                    estPriceHeader.text = "Est. price/TB"
                    purchasableHeader.text = "Purchasable"
                    tvPurchaseable.text = purchasable.format() + " TB"
                } else if (metric == CONTRACT) {
                    estPriceHeader.text = "Est. price"
                    purchasableHeader.text = "Purchasable"
                    tvPurchaseable.text = purchasable.format()
                }

                tvPrice.text = price.toSC().format() + currency
                tvSpent.text = spent.toSC().format() + currency
            }
        }

        vm.spending.observe(this) {
            dataSet.values[0].y = it.uploadspending.toSC().toFloat()
            dataSet.values[1].y = it.downloadspending.toSC().toFloat()
            dataSet.values[2].y = it.storagespending.toSC().toFloat()
            dataSet.values[3].y = it.contractspending.toSC().toFloat()
            dataSet.values[4].y = it.unspent.toSC().toFloat()
            dataSet.notifyDataSetChanged()

            val x = vm.currentMetric.value.ordinal.toFloat()
            if (highlightedX != x) {
                highlightedX = x
                pieChart.highlightValue(x, 0)
            }
        }

        siadSource.isSiadLoaded.observe(this) {
            if (it)
                vm.refresh()
        }
    }

    override fun onShow() {
        vm.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_allowance, menu)
        currencyButton = menu.findItem(R.id.currency_sc)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        vm.currency.value = when (item.itemId) {
            R.id.currency_sc -> Currency.SC
            R.id.currency_usd -> Currency.USD
            else -> return false
        }
        return true
    }
}