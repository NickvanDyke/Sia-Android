/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class TransactionOutputData
@JsonCreator constructor(
        @JsonProperty(value = "id")
        val id: String,
        @JsonProperty(value = "fundtype")
        val fundtype: String,
        @JsonProperty(value = "maturityheight")
        val maturityheight: BigDecimal,
        @JsonProperty(value = "walletaddress")
        val walletaddress: Boolean,
        @JsonProperty(value = "relatedaddress")
        val relatedaddress: String,
        @JsonProperty(value = "value")
        val value: BigDecimal)