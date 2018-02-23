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
data class ScValueData @JsonCreator constructor(
    @JsonProperty(value = "USD")
    val UsdPerSc: BigDecimal,

    @JsonProperty(value = "EUR")
    val EurPerSc: BigDecimal,

    @JsonProperty(value = "GBP")
    val GbpPerSc: BigDecimal,

    @JsonProperty(value = "CHF")
    val ChfPerSc: BigDecimal,

    @JsonProperty(value = "CAD")
    val CadPerSc: BigDecimal,

    @JsonProperty(value = "AUD")
    val AudPerSc: BigDecimal,

    @JsonProperty(value = "CNY")
    val CnyPerSc: BigDecimal,

    @JsonProperty(value = "JPY")
    val JpyPerSc: BigDecimal,

    @JsonProperty(value = "INR")
    val InrPerSc: BigDecimal,

    @JsonProperty(value = "BRL")
    val BrlPerSc: BigDecimal
) {

    @PrimaryKey
    var timestamp = System.currentTimeMillis()
}