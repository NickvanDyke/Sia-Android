/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.data.wallet

import java.math.BigDecimal
import java.util.*

data class TransactionData(val transactionid: String = "",
                           val confirmationheight: BigDecimal = BigDecimal.ZERO,
                           val confirmationtimestamp: BigDecimal = BigDecimal.ZERO,
                           val inputs: ArrayList<TransactionInputData> = ArrayList(),
                           val outputs: ArrayList<TransactionOutputData> = ArrayList()) {

    val confirmed: Boolean by lazy { confirmationtimestamp != BigDecimal("18446744073709551615") }

    val netValue: BigDecimal by lazy {
        var net = BigDecimal.ZERO
        for (input in inputs)
            if (input.walletaddress)
                net -= input.value
        for (output in outputs)
            if (output.walletaddress)
                net += output.value
        net
    }

    val isNetZero: Boolean by lazy { netValue == BigDecimal.ZERO }

    val confirmationdate: Date by lazy { if (confirmed) Date((confirmationtimestamp * BigDecimal("1000")).toLong()) else Date() }
}