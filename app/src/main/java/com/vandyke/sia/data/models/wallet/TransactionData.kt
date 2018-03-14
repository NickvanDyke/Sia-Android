/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.vandyke.sia.util.UNCONFIRMED_TX_TIMESTAMP
import java.math.BigDecimal
import java.util.*

@Entity(tableName = "transactions")
data class TransactionData(
        @PrimaryKey
        val transactionid: String,
        val confirmationheight: BigDecimal,
        val confirmationtimestamp: BigDecimal,
        val netValue: BigDecimal
) {
    @Ignore
    val confirmed: Boolean = confirmationtimestamp != UNCONFIRMED_TX_TIMESTAMP

    @Ignore
    val confirmationDate: Date? = if (confirmed) Date((confirmationtimestamp * BigDecimal("1000")).toLong()) else null

    @Ignore
    val isNetZero: Boolean = netValue == BigDecimal.ZERO
}

/** The intermediate class used when Moshi deserializes transaction Json. See DataAdapters for more */
data class TransactionDataJson(
        val transactionid: String,
        val confirmationheight: BigDecimal,
        val confirmationtimestamp: BigDecimal,
        val inputs: List<TransactionInputData>? = null,
        val outputs: List<TransactionOutputData>? = null)