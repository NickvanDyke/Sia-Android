/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@Entity(tableName = "scValue")
@JsonIgnoreProperties(ignoreUnknown = true)
data class ScValueData
@JsonCreator constructor(
        @JsonProperty(value = "USD")
        val Usd: BigDecimal,

        @JsonProperty(value = "EUR")
        val Eur: BigDecimal,

        @JsonProperty(value = "GBP")
        val Gbp: BigDecimal,

        @JsonProperty(value = "CHF")
        val Chf: BigDecimal,

        @JsonProperty(value = "CAD")
        val Cad: BigDecimal,

        @JsonProperty(value = "AUD")
        val Aud: BigDecimal,

        @JsonProperty(value = "CNY")
        val Cny: BigDecimal,

        @JsonProperty(value = "JPY")
        val Jpy: BigDecimal,

        @JsonProperty(value = "INR")
        val Inr: BigDecimal,

        @JsonProperty(value = "BRL")
        val Brl: BigDecimal
) {
    @PrimaryKey
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