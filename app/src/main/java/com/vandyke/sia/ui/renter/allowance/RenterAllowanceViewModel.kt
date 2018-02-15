/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.allowance

import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.models.renter.PricesData
import com.vandyke.sia.data.models.renter.RenterFinancialMetricsData
import com.vandyke.sia.data.models.renter.RenterSettingsAllowanceData
import com.vandyke.sia.data.repository.RenterRepository
import com.vandyke.sia.ui.renter.allowance.RenterAllowanceViewModel.Metrics.*
import com.vandyke.sia.util.rx.*
import io.reactivex.Completable
import java.math.BigDecimal
import javax.inject.Inject


class RenterAllowanceViewModel
@Inject constructor(
        private val renterRepository: RenterRepository
) : ViewModel() {
    /* stuff for the currently displayed financial metric */
    val currentMetric = NonNullLiveData(STORAGE)
    val estimatedPrice = NonNullLiveData(BigDecimal.ZERO)

    val allowance = NonNullLiveData(RenterSettingsAllowanceData(BigDecimal.ZERO, 0, 0, 0))
    val spending = NonNullLiveData(RenterFinancialMetricsData(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))

    val activeTasks = NonNullLiveData(0)
    val refreshing = NonNullLiveData(false)
    val error = SingleLiveEvent<Throwable>()

    private var prices: PricesData = PricesData(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)

    init {
        renterRepository.mostRecentPrices()
                .io()
                .main()
                .subscribe({ prices = it; setDisplayedMetrics() }, ::onError)

        renterRepository.mostRecentAllowance()
                .io()
                .main()
                .subscribe({ allowance.value = it; setDisplayedMetrics() }, ::onError)

        renterRepository.mostRecentSpending()
                .io()
                .main()
                .subscribe({ spending.value = it; setDisplayedMetrics() }, ::onError)

        currentMetric.observeForevs {
            setDisplayedMetrics()
        }
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
    }

    private fun setDisplayedMetrics() {
        val estPrice = when (currentMetric.value) {
            UPLOAD -> prices.uploadterabyte
            DOWNLOAD -> prices.downloadterabyte
            STORAGE -> prices.storageterabytemonth
            CONTRACT -> prices.formcontracts
        }
        estimatedPrice.value = estPrice
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
}