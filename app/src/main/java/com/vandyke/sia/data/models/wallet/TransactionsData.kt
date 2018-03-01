/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

data class TransactionsData(
        val confirmedtransactions: List<TransactionDataApi>?,
        val unconfirmedtransactions: List<TransactionDataApi>?
) {
    val alltransactions: List<TransactionDataApi>
        get() = (confirmedtransactions ?: listOf()) + (unconfirmedtransactions ?: listOf())
}