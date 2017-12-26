/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.remote.data.wallet

import java.math.BigDecimal

data class WalletData(
        val encrypted: Boolean = false,
        val unlocked: Boolean = false,
        val rescanning: Boolean = false,
        val confirmedsiacoinbalance: BigDecimal = BigDecimal.ZERO,
        val unconfirmedoutgoingsiacoins: BigDecimal = BigDecimal.ZERO,
        val unconfirmedincomingsiacoins: BigDecimal = BigDecimal.ZERO,
        val siafundbalance: Int = 0,
        val siacoinclaimbalance: BigDecimal = BigDecimal.ZERO) {

    val unconfirmedsiacoinbalance: BigDecimal by lazy { unconfirmedincomingsiacoins - unconfirmedoutgoingsiacoins }
}