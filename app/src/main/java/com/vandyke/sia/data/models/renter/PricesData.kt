/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "prices")
data class PricesData(
        @PrimaryKey
        val timestamp: Long,
        val downloadterabyte: BigDecimal,
        val formcontracts: BigDecimal, /* this is for forming 50 contracts (the default number) */
        val storageterabytemonth: BigDecimal,
        val uploadterabyte: BigDecimal)

data class PricesDataJson(
        val downloadterabyte: BigDecimal,
        val formcontracts: BigDecimal,
        val storageterabytemonth: BigDecimal,
        val uploadterabyte: BigDecimal)