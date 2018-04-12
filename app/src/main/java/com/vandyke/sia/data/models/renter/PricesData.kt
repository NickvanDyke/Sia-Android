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
        val downloadterabyte: BigDecimal, /* this includes 3x redundancy */
        val formcontracts: BigDecimal, /* this is for forming 50 contracts (the default number) */
        val storageterabytemonth: BigDecimal, /* this includes 3x redundancy */
        val uploadterabyte: BigDecimal /* this includes 3x redundancy */) {
    @Transient
    val formOneContract = formcontracts / BigDecimal("50")

    @Transient
    val downloadOneTerabyte = downloadterabyte / BigDecimal("3")

    @Transient
    val storageOneTerabyteMonth = storageterabytemonth / BigDecimal("3")

    @Transient
    val uploadOneTerabyte = uploadterabyte / BigDecimal("3")
}

data class PricesDataJson(
        val downloadterabyte: BigDecimal,
        val formcontracts: BigDecimal,
        val storageterabytemonth: BigDecimal,
        val uploadterabyte: BigDecimal)