/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

data class ModuleData(
        val type: Module,
        val on: Boolean,
        val internalSize: Long = 0,
        val externalSize: Long = 0)

enum class Module(val text: String) {
    WALLET("Wallet"),
    RENTER("Renter"),
    CONSENSUS("Consensus"),
    GATEWAY("Gateway"),
    TRANSACTIONPOOL("Transaction pool")
}