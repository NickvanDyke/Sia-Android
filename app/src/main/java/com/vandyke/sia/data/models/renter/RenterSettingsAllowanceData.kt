/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "allowance")
data class RenterSettingsAllowanceData(
        val funds: BigDecimal,
        val hosts: Int,
        val period: Int,
        val renewwindow: Int) {
    /* we only want one entry in the db, so we make the primary key a constant */
    @PrimaryKey
    @Transient
    @ColumnInfo(name = "key")
    var key = 0
}