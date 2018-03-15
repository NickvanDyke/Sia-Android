/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.models.consensus.ConsensusDataJson
import com.vandyke.sia.data.models.renter.PricesData
import com.vandyke.sia.data.models.renter.PricesDataJson
import com.vandyke.sia.data.models.renter.RenterFinancialMetricsData
import com.vandyke.sia.data.models.renter.RenterFinancialMetricsDataJson
import com.vandyke.sia.data.models.wallet.*
import com.vandyke.sia.util.sumByBigDecimal
import java.math.BigDecimal

/** Sometimes the data returned by the API isn't in the ideal form for us. In that case, we have an
  * intermediate class representing exactly the JSON that's returned. Moshi first deserializes the JSON into
  * that intermediate class, and then uses these adapters to convert it to the type that we specify
  * in the Retrofit interface.
  *
  * The purpose of many of these is just to add a timestamp field. Unfortunately due to the way
  * Moshi works (as well as Room, since we'd be trying to use the same class with that), we can't just add
  * a transient field that's initialized to System.currentTimeMillis() */
class DataAdapters {
    /* For example, here: The Retrofit interface specifies a TransactionData type for the /wallet/transactions endpoint.
     * Moshi will use this adapter to create it. It first deserializes the JSON response into a TransactionDataJson object,
     * since that's the parameter type that this method takes. Then it uses this function to turn that into a TransactionData object. */
    @FromJson
    fun transactionDataFromJson(data: TransactionDataJson): TransactionData {
        return TransactionData(data.transactionid, data.confirmationheight, data.confirmationtimestamp,
                (data.outputs?.filter { it.walletaddress }?.sumByBigDecimal { it.value }
                        ?: BigDecimal.ZERO) -
                        (data.inputs?.filter { it.walletaddress }?.sumByBigDecimal { it.value }
                                ?: BigDecimal.ZERO))
    }

    /* We never serialize them back into JSON for sending, but Moshi requires that there be a matching @ToJson method */
    @ToJson
    fun TransactionDataToJson(data: TransactionData): TransactionDataJson {
        return TransactionDataJson(data.transactionid, data.confirmationheight, data.confirmationtimestamp, null, null)
    }


    @FromJson
    fun walletDataFromJson(data: WalletDataJson): WalletData {
        return WalletData(System.currentTimeMillis(), data.encrypted, data.unlocked, data.rescanning, data.confirmedsiacoinbalance,
                data.unconfirmedoutgoingsiacoins, data.unconfirmedincomingsiacoins, data.siafundbalance, data.siacoinclaimbalance,
                data.dustthreshold)
    }

    @ToJson
    fun walletDataToJson(data: WalletData): WalletDataJson {
        return WalletDataJson(data.encrypted, data.unlocked, data.rescanning, data.confirmedsiacoinbalance,
                data.unconfirmedoutgoingsiacoins, data.unconfirmedincomingsiacoins, data.siafundbalance, data.siacoinclaimbalance,
                data.dustthreshold)
    }

    @FromJson
    fun scValueDataFromJson(data: ScValueDataJson): ScValueData {
        return ScValueData(System.currentTimeMillis(), data.USD, data.EUR, data.GBP, data.CHF, data.CAD, data.AUD, data.CNY, data.JPY, data.INR, data.BRL)
    }

    @ToJson
    fun scValueDataToJson(data: ScValueData): ScValueDataJson {
        return ScValueDataJson(data.USD, data.EUR, data.GBP, data.CHF, data.CAD, data.AUD, data.CNY, data.JPY, data.INR, data.BRL)
    }

    @FromJson
    fun consensusDataFromJson(data: ConsensusDataJson): ConsensusData {
        return ConsensusData(System.currentTimeMillis(), data.synced, data.height, data.currentblock, data.difficulty)
    }

    @ToJson
    fun consensusDataToJson(data: ConsensusData): ConsensusDataJson {
        return ConsensusDataJson(data.synced, data.height, data.currentblock, data.difficulty)
    }

    @FromJson
    fun pricesDataFromJson(data: PricesDataJson): PricesData {
        return PricesData(System.currentTimeMillis(), data.downloadterabyte, data.formcontracts, data.storageterabytemonth, data.uploadterabyte)
    }

    @ToJson
    fun renterFinancialMetricsDataToJson(data: RenterFinancialMetricsData): RenterFinancialMetricsDataJson {
        return RenterFinancialMetricsDataJson(data.contractspending, data.downloadspending, data.storagespending, data.uploadspending, data.unspent)
    }

    @FromJson
    fun renterFinancialMetricsDataFromJson(data: RenterFinancialMetricsDataJson): RenterFinancialMetricsData {
        return RenterFinancialMetricsData(System.currentTimeMillis(), data.contractspending, data.downloadspending, data.storagespending, data.uploadspending, data.unspent)
    }
}