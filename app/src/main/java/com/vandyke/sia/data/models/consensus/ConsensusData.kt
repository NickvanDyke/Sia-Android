/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.consensus

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.vandyke.sia.util.SiaUtil
import java.math.BigDecimal

@Entity(tableName = "consensus")
data class ConsensusData(
        val synced: Boolean,
        val height: Int,
        val currentblock: String,
        val difficulty: BigDecimal) {

    @Transient
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    var timestamp = System.currentTimeMillis()

    val syncProgress: Double
        get() = height.toDouble() / SiaUtil.estimatedBlockHeightAt(System.currentTimeMillis() / 1000) * 100
}