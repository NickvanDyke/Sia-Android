/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "scValue")
data class ScValueData(
        @PrimaryKey
        val timestamp: Long,
        val USD: BigDecimal,
        val EUR: BigDecimal,
        val GBP: BigDecimal,
        val CHF: BigDecimal,
        val CAD: BigDecimal,
        val AUD: BigDecimal,
        val CNY: BigDecimal,
        val JPY: BigDecimal,
        val INR: BigDecimal,
        val BRL: BigDecimal
) {
    operator fun get(currency: String)= when (currency) {
        "USD" -> USD
        "EUR" -> EUR
        "GBP" -> GBP
        "CHF" -> CHF
        "CAD" -> CAD
        "AUD" -> AUD
        "CNY" -> CNY
        "JPY" -> JPY
        "INR" -> INR
        "BRL" -> BRL
        else -> throw IllegalArgumentException("Invalid currency passed to getValueForCurrency: $currency")
    }
}

data class ScValueDataJson(
        val USD: BigDecimal,
        val EUR: BigDecimal,
        val GBP: BigDecimal,
        val CHF: BigDecimal,
        val CAD: BigDecimal,
        val AUD: BigDecimal,
        val CNY: BigDecimal,
        val JPY: BigDecimal,
        val INR: BigDecimal,
        val BRL: BigDecimal)