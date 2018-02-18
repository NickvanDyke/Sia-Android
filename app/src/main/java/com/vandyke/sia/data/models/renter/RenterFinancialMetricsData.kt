/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal

@Entity(tableName = "spending")
data class RenterFinancialMetricsData(val contractspending: BigDecimal,
                                      val downloadspending: BigDecimal,
                                      val storagespending: BigDecimal,
                                      val uploadspending: BigDecimal,
                                      val unspent: BigDecimal) {
    @PrimaryKey
    @JsonIgnore(value = true)
    var timestamp = System.currentTimeMillis()
}