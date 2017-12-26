/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local.data.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import vandyke.siamobile.data.remote.data.wallet.TransactionData
import java.math.BigDecimal
import java.util.*

@Entity(tableName = "transactions")
data class Transaction(
        @PrimaryKey
        val transactionId: String,
        val confirmed: Boolean,
        val confirmationHeight: BigDecimal,
        val confirmationTimestamp: BigDecimal,
        val netValue: BigDecimal) {

    val isNetZero: Boolean by lazy { netValue == BigDecimal.ZERO }

    val confirmationDate: Date by lazy { if (confirmed) Date((confirmationTimestamp * BigDecimal("1000")).toLong()) else Date() }

    companion object {
        fun fromTransactionData(it: TransactionData) = Transaction(it.transactionid, it.confirmed, it.confirmationheight, it.confirmationtimestamp, it.netValue)
    }
}