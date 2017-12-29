/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.models.wallet

data class TransactionsData(val confirmedtransactions: List<TransactionData>? = listOf(),
                            val unconfirmedtransactions: List<TransactionData>? = listOf()) {
    val alltransactions by lazy { (confirmedtransactions?: listOf()) + (unconfirmedtransactions?: listOf()) }
}