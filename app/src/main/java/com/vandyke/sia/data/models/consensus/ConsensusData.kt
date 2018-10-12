/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.consensus

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vandyke.sia.util.SiaUtil
import java.math.BigDecimal

@Entity(tableName = "consensus")
data class ConsensusData(
        @PrimaryKey
        val timestamp: Long,
        val synced: Boolean,
        val height: Int,
        val currentblock: String,
        val difficulty: BigDecimal) {

    val syncProgress: Double
        get() = height.toDouble() / SiaUtil.estimatedBlockHeightAt(System.currentTimeMillis() / 1000) * 100
}

data class ConsensusDataJson(
        val synced: Boolean,
        val height: Int,
        val currentblock: String,
        val difficulty: BigDecimal)