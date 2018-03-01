/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "prices")
data class PricesData(
        val downloadterabyte: BigDecimal,
        val formcontracts: BigDecimal,
        val storageterabytemonth: BigDecimal,
        val uploadterabyte: BigDecimal
) {
    @PrimaryKey
    @Transient
    @ColumnInfo(name = "timestamp")
    var timestamp = System.currentTimeMillis()
}