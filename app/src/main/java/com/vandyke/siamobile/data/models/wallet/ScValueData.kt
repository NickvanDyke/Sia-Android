/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.models.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@Entity(tableName = "scValue")
@JsonIgnoreProperties(ignoreUnknown = true)
data class ScValueData @JsonCreator constructor(
        @JsonProperty(value = "price_usd")
        val UsdPerSc: BigDecimal) {

    @PrimaryKey
    var timestamp = System.currentTimeMillis()
}