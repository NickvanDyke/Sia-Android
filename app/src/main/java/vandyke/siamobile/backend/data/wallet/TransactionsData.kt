/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.data.wallet

data class TransactionsData(val confirmedtransactions: ArrayList<TransactionData>? = ArrayList(),
                            val unconfirmedtransactions: ArrayList<TransactionData>? = ArrayList()) {
    val alltransactions by lazy { (confirmedtransactions?: ArrayList()) + (unconfirmedtransactions?: ArrayList()) }
}