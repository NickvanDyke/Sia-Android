/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.data.data.wallet

data class TransactionsData(val confirmedtransactions: List<TransactionData>? = listOf(),
                            val unconfirmedtransactions: List<TransactionData>? = listOf()) {
    val alltransactions by lazy { (confirmedtransactions?: listOf()) + (unconfirmedtransactions?: listOf()) }
}