/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.helpers

import com.vandyke.sia.data.models.wallet.ScValueData
import java.math.BigDecimal

object ScValueHelper {

    const val USD = "USD"
    const val EUR = "EUR"
    const val GBP = "GBP"
    const val CHF = "CHF"
    const val CAD = "CAD"
    const val AUD = "AUD"
    const val CNY = "CNY"
    const val JPY = "JPY"
    const val INR = "INR"
    const val BRL = "BRL"

    fun getValueByCurrency(currency: String, scValue: ScValueData): BigDecimal {
        return when (currency) {
            USD -> scValue.UsdPerSc
            EUR -> scValue.EurPerSc
            GBP -> scValue.ChfPerSc
            CHF -> scValue.ChfPerSc
            CAD -> scValue.CadPerSc
            AUD -> scValue.AudPerSc
            CNY -> scValue.CnyPerSc
            JPY -> scValue.JpyPerSc
            INR -> scValue.InrPerSc
            BRL -> scValue.BrlPerSc
            else -> scValue.UsdPerSc
        }
    }
}