/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json
import com.vandyke.sia.util.UNCONFIRMED_TX_TIMESTAMP
import java.math.BigDecimal
import java.util.*

@Entity(tableName = "transactions")
data class TransactionData(
        @PrimaryKey
        @Json(name = "transactionid")
        val transactionId: String,
        @Json(name = "confirmationheight")
        val confirmationHeight: BigDecimal,
        @Json(name = "confirmationtimestamp")
        val confirmationTimestamp: BigDecimal,
        @Ignore
        @Json(name = "inputs")
        val inputs: List<TransactionInputData>? = null,
        @Ignore
        @Json(name = "outputs")
        val outputs: List<TransactionOutputData>? = null) {


    constructor(transactionId: String,
                confirmationHeight: BigDecimal,
                confirmationTimestamp: BigDecimal) :
            this(transactionId, confirmationHeight, confirmationTimestamp, null, null)

    @Transient
    var confirmed: Boolean = confirmationTimestamp != UNCONFIRMED_TX_TIMESTAMP

    // TODO: I'm pretty sure this gets calculated even when the object is loaded from the database. Not ideal.
    @Transient
    var netValue: BigDecimal = run {
        var net = BigDecimal.ZERO
        inputs?.filter { it.walletaddress }?.forEach { net -= it.value }
        outputs?.filter { it.walletaddress }?.forEach { net += it.value }
        net
    }

    val isNetZero: Boolean
        get() = netValue == BigDecimal.ZERO

    @Ignore
    val confirmationDate: Date = if (confirmed) Date((confirmationTimestamp * BigDecimal("1000")).toLong()) else Date()
}