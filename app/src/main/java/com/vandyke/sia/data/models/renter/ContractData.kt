/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import com.vandyke.sia.data.models.wallet.TransactionData
import java.math.BigDecimal

data class ContractData(
        val endheight: Int,
        val id: String,
        val netaddress: String,
        val lasttransaction: TransactionData,
        val renterfunds: BigDecimal,
        val size: BigDecimal)