/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.vandyke.sia.util.UNCONFIRMED_TX_TIMESTAMP
import com.vandyke.sia.util.sumByBigDecimal
import java.math.BigDecimal
import java.util.*

data class TransactionDataApi(
        val transactionid: String,
        val confirmationheight: BigDecimal,
        val confirmationtimestamp: BigDecimal,
        val inputs: List<TransactionInputData>? = null,
        val outputs: List<TransactionOutputData>? = null
) {
    fun toDbTransaction() = TransactionData(transactionid, confirmationheight, confirmationtimestamp,
            (outputs?.filter { it.walletaddress }?.sumByBigDecimal { it.value }
                    ?: BigDecimal.ZERO) -
                    (inputs?.filter { it.walletaddress }?.sumByBigDecimal { it.value }
                            ?: BigDecimal.ZERO))
}

@Entity(tableName = "transactions")
data class TransactionData(
        @PrimaryKey
        val transactionid: String,
        val confirmationheight: BigDecimal,
        val confirmationtimestamp: BigDecimal,
        val netValue: BigDecimal
) {
    var confirmed: Boolean = confirmationtimestamp != UNCONFIRMED_TX_TIMESTAMP

    @Ignore
    val confirmationDate: Date? = if (confirmed) Date((confirmationtimestamp * BigDecimal("1000")).toLong()) else null

    @Ignore
    val isNetZero: Boolean = netValue == BigDecimal.ZERO
}