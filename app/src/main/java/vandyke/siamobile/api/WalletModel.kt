package vandyke.siamobile.api

import java.math.BigDecimal

data class WalletModel(
        var encrypted: Boolean,
        var unlocked: Boolean,
        var rescanning: Boolean,
        var confirmedsiacoinbalance: BigDecimal,
        var unconfirmedoutgoingsiacoins: BigDecimal,
        var unconfirmedincomingsiacoins: BigDecimal,
        var siafundbalance: BigDecimal,
        var siacoinclaimbalance: BigDecimal) {

    var unconfirmedsiacoinbalance: BigDecimal
        get() = unconfirmedincomingsiacoins - unconfirmedoutgoingsiacoins

    init {
        unconfirmedsiacoinbalance = unconfirmedincomingsiacoins - unconfirmedoutgoingsiacoins
    }
}