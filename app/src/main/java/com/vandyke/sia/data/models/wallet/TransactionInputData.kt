/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import java.math.BigDecimal

data class TransactionInputData(
        val parentid: String,
        val fundtype: String,
        val walletaddress: Boolean,
        val relatedaddress: String,
        val value: BigDecimal)