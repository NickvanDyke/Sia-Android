/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class TransactionInputData
@JsonCreator constructor(
        @JsonProperty(value = "parentid")
        val parentid: String,
        @JsonProperty(value = "fundtype")
        val fundtype: String,
        @JsonProperty(value = "walletaddress")
        val walletaddress: Boolean,
        @JsonProperty(value = "relatedaddress")
        val relatedaddress: String,
        @JsonProperty(value = "value")
        val value: BigDecimal)