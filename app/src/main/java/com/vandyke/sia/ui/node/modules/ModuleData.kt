/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

import java.math.BigDecimal

data class ModuleData(
        val type: Module,
        val on: Boolean,
        val internalSize: BigDecimal = BigDecimal.ZERO,
        val externalSize: BigDecimal = BigDecimal.ZERO)

enum class Module(val text: String) {
    WALLET("Wallet"),
    RENTER("Renter"),
    CONSENSUS("Consensus"),
    GATEWAY("Gateway"),
    TRANSACTIONPOOL("Transaction pool")
}