/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "contracts")
data class ContractData(
        @PrimaryKey
        val id: String,
        val uploadspending: BigDecimal,
        val downloadspending: BigDecimal,
        val storagespending: BigDecimal,
        val totalcost: BigDecimal,
        val fees: BigDecimal,
        val renterfunds: BigDecimal,
        val startheight: Int,
        val endheight: Int,
        val netaddress: String,
        val size: BigDecimal,
        val goodforupload: Boolean,
        val goodforrenew: Boolean)