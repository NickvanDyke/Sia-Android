/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json
import java.math.BigDecimal

@Entity(tableName = "scValue")
data class ScValueData(
        @Json(name = "USD")
        val Usd: BigDecimal,

        @Json(name = "EUR")
        val Eur: BigDecimal,

        @Json(name = "GBP")
        val Gbp: BigDecimal,

        @Json(name = "CHF")
        val Chf: BigDecimal,

        @Json(name = "CAD")
        val Cad: BigDecimal,

        @Json(name = "AUD")
        val Aud: BigDecimal,

        @Json(name = "CNY")
        val Cny: BigDecimal,

        @Json(name = "JPY")
        val Jpy: BigDecimal,

        @Json(name = "INR")
        val Inr: BigDecimal,

        @Json(name = "BRL")
        val Brl: BigDecimal
) {
    @PrimaryKey
    @Transient
    @ColumnInfo(name = "timestamp")
    var timestamp = System.currentTimeMillis()

    fun getValueForCurrency(currency: String): BigDecimal = when (currency) {
        USD -> Usd
        EUR -> Eur
        GBP -> Chf
        CHF -> Chf
        CAD -> Cad
        AUD -> Aud
        CNY -> Cny
        JPY -> Jpy
        INR -> Inr
        BRL -> Brl
        else -> throw IllegalArgumentException("Invalid currency passed to getValueForCurrency: $currency")
    }

    companion object {
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
    }
}