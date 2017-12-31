/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import java.math.BigDecimal

data class TransactionOutputData(val id: String = "",
                                 val fundtype: String = "",
                                 val maturityheight: BigDecimal = BigDecimal.ZERO,
                                 val walletaddress: Boolean = false,
                                 val relatedaddress: String = "",
                                 val value: BigDecimal = BigDecimal.ZERO)