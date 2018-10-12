/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currentPeriod")
data class CurrentPeriodData(val currentPeriod: Int) {
    /* we only want one entry in the db, so we make the primary key a constant */
    @PrimaryKey
    @Transient
    @ColumnInfo(name = "key")
    var key = 0
}