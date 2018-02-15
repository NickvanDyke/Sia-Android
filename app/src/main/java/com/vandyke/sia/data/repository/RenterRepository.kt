/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.remote.SiaApiInterface
import io.reactivex.Completable
import java.math.BigDecimal
import javax.inject.Inject


class RenterRepository
@Inject constructor(
        val api: SiaApiInterface,
        val db: AppDatabase
) {
    fun setAllowance(funds: BigDecimal, hosts: Int, period: Int, renewWindow: Int) = api.renter(funds, hosts, period, renewWindow)

    fun updateAllowanceAndMetrics(): Completable = api.renter()
            .doOnSuccess {
                db.spendingDao().insertReplaceOnConflict(it.financialmetrics)
                db.allowanceDao().insertReplaceOnConflict(it.settings.allowance)
                // TODO: what to do with currentperiod? Don't want to need a table just for it...
            }
            .toCompletable()

    fun updatePrices(): Completable = api.renterPrices()
            .doOnSuccess { db.pricesDao().insertReplaceOnConflict(it) }
            .toCompletable()

    fun mostRecentPrices() = db.pricesDao().mostRecent()

    fun mostRecentAllowance() = db.allowanceDao().onlyEntry()

    fun mostRecentSpending() = db.spendingDao().mostRecent()
}