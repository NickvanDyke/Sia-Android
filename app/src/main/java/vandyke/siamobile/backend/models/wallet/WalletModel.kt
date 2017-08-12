/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.models.wallet

import java.math.BigDecimal

data class WalletModel(
        val encrypted: Boolean = false,
        val unlocked: Boolean = false,
        val rescanning: Boolean = false,
        val confirmedsiacoinbalance: BigDecimal = BigDecimal.ZERO,
        val unconfirmedoutgoingsiacoins: BigDecimal = BigDecimal.ZERO,
        val unconfirmedincomingsiacoins: BigDecimal = BigDecimal.ZERO,
        val siafundbalance: BigDecimal = BigDecimal.ZERO,
        val siacoinclaimbalance: BigDecimal = BigDecimal.ZERO) {

    val unconfirmedsiacoinbalance: BigDecimal by lazy { unconfirmedincomingsiacoins - unconfirmedoutgoingsiacoins }
}