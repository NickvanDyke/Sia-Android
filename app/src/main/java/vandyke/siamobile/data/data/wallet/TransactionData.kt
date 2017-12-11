/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.data.data.wallet

import java.math.BigDecimal
import java.util.*

data class TransactionData(val transactionid: String = "",
                           val confirmationheight: BigDecimal = BigDecimal.ZERO,
                           val confirmationtimestamp: BigDecimal = BigDecimal.ZERO,
                           val inputs: List<TransactionInputData> = listOf(),
                           val outputs: List<TransactionOutputData> = listOf()) {

    val confirmed: Boolean by lazy { confirmationtimestamp != BigDecimal("18446744073709551615") }

    val netValue: BigDecimal by lazy {
        var net = BigDecimal.ZERO
        inputs.filter { it.walletaddress }.forEach { net -= it.value }
        outputs.filter { it.walletaddress }.forEach { net += it.value }
        net
    }

    val isNetZero: Boolean by lazy { netValue == BigDecimal.ZERO }

    val confirmationdate: Date by lazy { if (confirmed) Date((confirmationtimestamp * BigDecimal("1000")).toLong()) else Date() }
}