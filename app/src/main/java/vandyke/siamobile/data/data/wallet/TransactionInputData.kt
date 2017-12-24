/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.data.wallet

import java.math.BigDecimal

data class TransactionInputData(val parentid: String = "",
                                val fundtype: String = "",
                                val walletaddress: Boolean = false,
                                val relatedaddress: String = "",
                                val value: BigDecimal = BigDecimal.ZERO)