/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.local.AppDatabase
import com.vandyke.sia.data.models.renter.ContractData
import com.vandyke.sia.data.models.renter.CurrentPeriodData
import com.vandyke.sia.data.models.renter.RenterSettingsAllowanceData
import com.vandyke.sia.data.remote.SiaApi
import com.vandyke.sia.util.diffWith
import com.vandyke.sia.util.rx.inDbTransaction
import io.reactivex.Completable
import io.reactivex.rxkotlin.zipWith
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RenterRepository
@Inject constructor(
        private val api: SiaApi,
        private val db: AppDatabase
) {
    fun setAllowance(funds: BigDecimal, hosts: Int, period: Int, renewWindow: Int): Completable = api.renter(funds, hosts, period, renewWindow)
            .doOnComplete { db.allowanceDao().insertReplaceOnConflict(RenterSettingsAllowanceData(funds, hosts, period, renewWindow)) }

    fun updateAllowanceAndMetrics(): Completable = api.renter()
            .doOnSuccess {
                db.spendingDao().insertReplaceOnConflict(it.financialmetrics)
                db.allowanceDao().insertReplaceOnConflict(it.settings.allowance)
                db.currentPeriodDao().insertReplaceOnConflict(CurrentPeriodData(it.currentperiod))
            }
            .toCompletable()

    fun updatePrices(): Completable = api.renterPrices()
            .doOnSuccess { db.pricesDao().insertReplaceOnConflict(it) }
            .toCompletable()

    fun updateContracts(): Completable = api.renterContracts()
            .map { it.contracts.sortedBy(ContractData::id) }
            .zipWith(db.contractDao().getAllById())
            .doOnSuccess { (apiContracts, dbContracts) ->
                apiContracts.diffWith(
                        dbContracts,
                        ContractData::id,
                        { apiContract, dbContract ->
                            if (apiContract != dbContract)
                                db.contractDao().insertReplaceOnConflict(apiContract)
                        },
                        { db.contractDao().insertAbortOnConflict(it) },
                        { db.contractDao().delete(it) })
            }
            .toCompletable()
            .inDbTransaction(db)

    fun mostRecentPrices() = db.pricesDao().mostRecent()

    fun allowance() = db.allowanceDao().onlyEntry()

    fun mostRecentSpending() = db.spendingDao().mostRecent()

    fun currentPeriod() = db.currentPeriodDao().onlyEntry()

    fun contracts() = db.contractDao().all()
}