/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.data.wallet

import java.math.BigDecimal

data class TransactionInputData(val parentid: String = "",
                                val fundtype: String = "",
                                val walletaddress: Boolean = false,
                                val relatedaddress: String = "",
                                val value: BigDecimal = BigDecimal.ZERO)