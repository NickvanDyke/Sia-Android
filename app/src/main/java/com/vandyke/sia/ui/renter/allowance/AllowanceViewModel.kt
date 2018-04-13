/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.allowance

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.models.renter.CurrentPeriodData
import com.vandyke.sia.data.models.renter.PricesData
import com.vandyke.sia.data.models.renter.RenterFinancialMetricsData
import com.vandyke.sia.data.models.renter.RenterSettingsAllowanceData
import com.vandyke.sia.data.models.wallet.ScValueData
import com.vandyke.sia.data.repository.ConsensusRepository
import com.vandyke.sia.data.repository.RenterRepository
import com.vandyke.sia.data.repository.ScValueRepository
import com.vandyke.sia.ui.renter.allowance.AllowanceViewModel.Currency.FIAT
import com.vandyke.sia.ui.renter.allowance.AllowanceViewModel.Currency.SC
import com.vandyke.sia.ui.renter.allowance.AllowanceViewModel.Metrics.*
import com.vandyke.sia.util.rx.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.functions.Function3
import java.math.BigDecimal
import javax.inject.Inject


class AllowanceViewModel
@Inject constructor(
        private val renterRepository: RenterRepository,
        private val scValueRepository: ScValueRepository,
        private val consensusRepository: ConsensusRepository
) : ViewModel() {
    val currency = MutableNonNullLiveData(Prefs.allowanceCurrency)

    val currentMetric = MutableNonNullLiveData(STORAGE)
    val currentMetricValues = MutableLiveData<MetricValues>()
    val remainingPeriod = MutableLiveData<Int>()

    val allowance = MutableLiveData<RenterSettingsAllowanceData>()
    val spending = MutableLiveData<RenterFinancialMetricsData>()
    val prices = MutableLiveData<PricesData>()
    val scValue = MutableLiveData<ScValueData>()

    val activeTasks = MutableNonNullLiveData(0)
    val refreshing = MutableNonNullLiveData(false)
    val error = MutableSingleLiveEvent<Throwable>()

    init {
        // maybe merge these together
        renterRepository.allowance()
                .io()
                .main()
                .subscribe(allowance::setValue, ::onError)

        renterRepository.mostRecentPrices()
                .io()
                .main()
                .subscribe({ prices.value = it; setDisplayedMetricValues() }, ::onError)

        renterRepository.mostRecentSpending()
                .io()
                .main()
                .subscribe({ spending.value = it; setDisplayedMetricValues() }, ::onError)

        scValueRepository.mostRecent()
                .io()
                .main()
                .subscribe({ scValue.value = it; setDisplayedMetricValues() }, ::onError)

        currency.observeForevs { Prefs.allowanceCurrency = it; setDisplayedMetricValues() }
        currentMetric.observeForevs { setDisplayedMetricValues() }

        /* if one of the must-be-non-zero allowance values is zero, set it to a default value */
        allowance.observeForevs { (amt, hosts, period, renew) ->
            if (hosts == 0 || period == 0 || renew == 0)
                setAllowance(amt,
                        if (hosts == 0) 50 else hosts,
                        if (period == 0) 12000 else period,
                        if (renew == 0) 4000 else renew)
        }

        Flowable.combineLatest(
                renterRepository.currentPeriod(),
                consensusRepository.consensus(),
                allowance.toFlowable(),
                Function3 { currentPeriod: CurrentPeriodData, consensus: ConsensusData, allowance: RenterSettingsAllowanceData ->
                    val periodEndsAt = currentPeriod.currentPeriod + allowance.period
                    return@Function3 periodEndsAt - consensus.height
                })
                .io()
                .main()
                .subscribe(remainingPeriod::setValue, ::onError)
    }

    fun refresh() {
        Completable.mergeArrayDelayError(
                renterRepository.updatePrices(),
                renterRepository.updateAllowanceAndMetrics(),
                consensusRepository.updateConsensus())
                .io()
                .main()
                .track(refreshing)
                .subscribe({}, ::onError)

        /* we don't include this in the refresh task because it's remote and less reliable and speedy. And also not as integral. */
        scValueRepository.updateScValue()
                .io()
                .main()
                .subscribe({}, ::onError)
    }

    fun setAllowance(funds: BigDecimal? = allowance.value?.funds,
                     hosts: Int? = allowance.value?.hosts,
                     period: Int? = allowance.value?.period,
                     renewWindow: Int? = allowance.value?.renewwindow) {
        if (funds == null || hosts == null || period == null || renewWindow == null) {
            onError(IllegalArgumentException("Null value passed to setAllowance"))
            return
        }

        renterRepository.setAllowance(funds, hosts, period, renewWindow)
                .io()
                .main()
                .track(activeTasks)
                .subscribe(::refresh, ::onError)
    }

    fun toggleDisplayedCurrency() {
        currency.value = if (currency.value == SC) FIAT else SC
    }

    private fun setDisplayedMetricValues() {
        val conversionRate = if (currency.value == SC)
            BigDecimal("1.00")
        else
            with(scValue.value ?: return) {
                when (currency.value) {
                    SC -> BigDecimal("1.00") /* using the ONE constant results in rounding when dividing later. Don't know why */
                    FIAT -> this[Prefs.fiatCurrency]
                }
            }

        val price = with(prices.value ?: return) {
            when (currentMetric.value) {
                UPLOAD -> uploadOneTerabyte
                DOWNLOAD -> downloadOneTerabyte
                STORAGE -> storageOneTerabyteMonth
                CONTRACT -> formOneContract
                UNSPENT -> BigDecimal.ZERO
            }
        } * conversionRate

        val spendingData = spending.value ?: return
        val spent = with(spendingData) {
            when (currentMetric.value) {
                UPLOAD -> uploadspending
                DOWNLOAD -> downloadspending
                STORAGE -> storagespending
                CONTRACT -> contractspending
                UNSPENT -> unspent
            }
        } * conversionRate

        val purchasable = when {
            price.toInt() == 0 -> BigDecimal.ZERO
            else -> spendingData.unspent * conversionRate / price
        }

        currentMetricValues.value = MetricValues(
                price,
                spent,
                purchasable)
    }

    private fun onError(t: Throwable) {
        error.value = t
    }

    enum class Metrics(val text: String) {
        UPLOAD("Uploading"),
        DOWNLOAD("Downloading"),
        STORAGE("Storage"),
        CONTRACT("Contracts"),
        UNSPENT("Unspent")
    }

    enum class Currency {
        SC,
        FIAT
    }

    data class MetricValues(val price: BigDecimal, val spent: BigDecimal, val purchasable: BigDecimal)
}