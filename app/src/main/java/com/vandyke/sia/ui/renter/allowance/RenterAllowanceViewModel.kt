/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.allowance

import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.models.renter.PricesData
import com.vandyke.sia.data.models.renter.RenterFinancialMetricsData
import com.vandyke.sia.data.models.renter.RenterSettingsAllowanceData
import com.vandyke.sia.data.models.wallet.ScValueData
import com.vandyke.sia.data.repository.RenterRepository
import com.vandyke.sia.data.repository.ScValueRepository
import com.vandyke.sia.ui.renter.allowance.RenterAllowanceViewModel.Currency.SC
import com.vandyke.sia.ui.renter.allowance.RenterAllowanceViewModel.Currency.USD
import com.vandyke.sia.ui.renter.allowance.RenterAllowanceViewModel.Metrics.*
import com.vandyke.sia.util.rx.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.functions.Function3
import java.math.BigDecimal
import javax.inject.Inject


class RenterAllowanceViewModel
@Inject constructor(
        private val renterRepository: RenterRepository,
        private val scValueRepository: ScValueRepository
) : ViewModel() {
    val currency = NonNullLiveData(Prefs.allowanceCurrency)

    val currentMetric = NonNullLiveData(STORAGE)
    val currentMetricValues = NonNullLiveData(MetricValues(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))

    val allowanceSettings = NonNullLiveData(RenterSettingsAllowanceData(BigDecimal.ZERO, 0, 0, 0))

    val activeTasks = NonNullLiveData(0)
    val refreshing = NonNullLiveData(false)
    val error = SingleLiveEvent<Throwable>()

    private var cached: Triple<PricesData, RenterFinancialMetricsData, ScValueData>? = null

    init {
        renterRepository.mostRecentAllowance()
                .io()
                .main()
                .subscribe(allowanceSettings::setValue, ::onError)

        Flowable.combineLatest(
                renterRepository.mostRecentPrices(),
                renterRepository.mostRecentSpending(),
                scValueRepository.mostRecent(),
                Function3 { prices: PricesData, spending: RenterFinancialMetricsData, scValue: ScValueData ->
                    Triple(prices, spending, scValue)
                })
                .io()
                .main()
                .subscribe({ cached = it; setDisplayedMetrics() }, ::onError)

        currentMetric.observeForevs { setDisplayedMetrics() }

        currency.observeForevs { Prefs.allowanceCurrency = it; setDisplayedMetrics() }
    }

    fun refresh() {
        Completable.mergeArrayDelayError(
                renterRepository.updatePrices(),
                renterRepository.updateAllowanceAndMetrics())
                .io()
                .main()
                .track(activeTasks)
                .track(refreshing)
                .subscribe({}, ::onError)

        /* we don't include this in the refresh task because it's remote and less reliable and speedy. And also not as integral. */
        scValueRepository.updateScValue()
                .io()
                .main()
                .subscribe({}, ::onError)
    }

    fun setAllowance(funds: BigDecimal = allowanceSettings.value.funds,
                     hosts: Int = allowanceSettings.value.hosts,
                     period: Int = allowanceSettings.value.period,
                     renewWindow: Int = allowanceSettings.value.renewwindow) {
        renterRepository.setAllowance(funds, hosts, period, renewWindow)
                .io()
                .main()
                .track(activeTasks)
                .subscribe(::refresh, ::onError)
    }

    private fun setDisplayedMetrics() {
        val (prices, spending, scValue) = cached ?: return
        val conversionRate = when (currency.value) {
            SC -> BigDecimal.ONE
            USD -> scValue.UsdPerSc
        }

        val price = with(prices) {
            when (currentMetric.value) {
                UPLOAD -> uploadterabyte
                DOWNLOAD -> downloadterabyte
                STORAGE -> storageterabytemonth
                CONTRACT -> formcontracts
            }
        } * conversionRate

        val spent = with(spending) {
            when (currentMetric.value) {
                UPLOAD -> uploadspending
                DOWNLOAD -> downloadspending
                STORAGE -> storagespending
                CONTRACT -> contractspending
            }
        } * conversionRate

        currentMetricValues.value = MetricValues(price, spent, spending.unspent / price)
    }

    private fun onError(t: Throwable) {
        error.value = t
    }

    enum class Metrics(val text: String) {
        UPLOAD("upload"),
        DOWNLOAD("download"),
        STORAGE("storage"),
        CONTRACT("contract")
    }

    enum class Currency(val text: String) {
        SC("SC"),
        USD("USD")
    }

    data class MetricValues(val price: BigDecimal, val spent: BigDecimal, val remaining: BigDecimal)
}