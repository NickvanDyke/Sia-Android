/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "spending")
data class RenterFinancialMetricsData(
        @PrimaryKey
        val timestamp: Long,
        val uploadspending: BigDecimal,
        val downloadspending: BigDecimal,
        val storagespending: BigDecimal,
        val contractspending: BigDecimal,
        val unspent: BigDecimal)

data class RenterFinancialMetricsDataJson(
        val uploadspending: BigDecimal,
        val downloadspending: BigDecimal,
        val storagespending: BigDecimal,
        val contractspending: BigDecimal,
        val unspent: BigDecimal)