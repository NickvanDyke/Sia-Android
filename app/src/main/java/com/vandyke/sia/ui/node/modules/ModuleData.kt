/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

import java.io.File

data class ModuleData(
        val type: Module,
        val enabled: Boolean,
        val directories: List<File>)

enum class Module(val text: String) {
    WALLET("Wallet"),
    RENTER("Renter"),
    CONSENSUS("Consensus"),
    GATEWAY("Gateway"),
    TRANSACTIONPOOL("Transaction pool")
}