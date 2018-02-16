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
import com.vandyke.sia.ui.renter.allowance.AllowanceViewModel.Currency.SC
import com.vandyke.sia.ui.renter.allowance.AllowanceViewModel.Currency.USD
import com.vandyke.sia.ui.renter.allowance.AllowanceViewModel.Metrics.*
import com.vandyke.sia.util.rx.*
import io.reactivex.Completable
import java.math.BigDecimal
import javax.inject.Inject


class AllowanceViewModel
@Inject constructor(
        private val renterRepository: RenterRepository,
        private val scValueRepository: ScValueRepository
) : ViewModel() {
    val currency = NonNullLiveData(Prefs.allowanceCurrency)

    val currentMetric = NonNullLiveData(STORAGE)
    val currentMetricValues = NonNullLiveData(MetricValues(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))

    val allowance = NonNullLiveData(RenterSettingsAllowanceData(BigDecimal.ZERO, 0, 0, 0))
    val spending = NonNullLiveData(RenterFinancialMetricsData(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
    val prices = NonNullLiveData(PricesData(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
    val scValue = NonNullLiveData(ScValueData(BigDecimal.ZERO))

    val activeTasks = NonNullLiveData(0)
    val refreshing = NonNullLiveData(false)
    val error = SingleLiveEvent<Throwable>()

    private var cached: Triple<PricesData, RenterFinancialMetricsData, ScValueData>? = null

    init {
        renterRepository.mostRecentAllowance()
                .io()
                .main()
                .subscribe(allowance::setValue, ::onError)

        renterRepository.mostRecentPrices()
                .io()
                .main()
                .subscribe({ prices.value = it; setDisplayedMetrics() })

        renterRepository.mostRecentSpending()
                .io()
                .main()
                .subscribe({ spending.value = it; setDisplayedMetrics() }, ::onError)

        scValueRepository.mostRecent()
                .io()
                .main()
                .subscribe({ scValue.value = it; setDisplayedMetrics() }, ::onError)

        currency.observeForevs { Prefs.allowanceCurrency = it; setDisplayedMetrics() }
        currentMetric.observeForevs { setDisplayedMetrics() }
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

    fun setAllowance(funds: BigDecimal = allowance.value.funds,
                     hosts: Int = allowance.value.hosts,
                     period: Int = allowance.value.period,
                     renewWindow: Int = allowance.value.renewwindow) {
        renterRepository.setAllowance(funds, hosts, period, renewWindow)
                .io()
                .main()
                .track(activeTasks)
                .subscribe(::refresh, ::onError)
    }

    private fun setDisplayedMetrics() {
        val conversionRate = with(scValue.value) {
            when (currency.value) {
                SC -> BigDecimal.ONE
                USD -> UsdPerSc
            }
        }

        val price = with(prices.value) {
            when (currentMetric.value) {
                UPLOAD -> uploadterabyte
                DOWNLOAD -> downloadterabyte
                STORAGE -> storageterabytemonth
                CONTRACT -> formcontracts
                UNSPENT -> BigDecimal.ZERO
            }
        } * conversionRate

        val spent = with(spending.value) {
            when (currentMetric.value) {
                UPLOAD -> uploadspending
                DOWNLOAD -> downloadspending
                STORAGE -> storagespending
                CONTRACT -> contractspending
                UNSPENT -> unspent
            }
        } * conversionRate

        currentMetricValues.value = MetricValues(
                price,
                spent,
                if (price.toInt() == 0) BigDecimal.ZERO else spending.value.unspent / price)
    }

    private fun onError(t: Throwable) {
        error.value = t
    }

    enum class Metrics(val text: String) {
        UPLOAD("Uploading"),
        DOWNLOAD("Downloading"),
        STORAGE("Storage"),
        CONTRACT("Form contracts"),
        UNSPENT("Unspent")
    }

    enum class Currency(val text: String) {
        SC("SC"),
        USD("USD")
    }

    data class MetricValues(val price: BigDecimal, val spent: BigDecimal, val purchasable: BigDecimal)
}