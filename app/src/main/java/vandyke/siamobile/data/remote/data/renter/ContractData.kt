/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.remote.data.renter

import vandyke.siamobile.data.remote.data.wallet.TransactionData
import java.math.BigDecimal

data class ContractData(val endheight: Int = 0,
                        val id: String = "",
                        val netaddress: String = "",
                        val lasttransaction: TransactionData,
                        val renterfunds: BigDecimal = BigDecimal.ZERO,
                        val size: BigDecimal = BigDecimal.ZERO)