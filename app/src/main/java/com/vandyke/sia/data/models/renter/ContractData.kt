/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import android.arch.persistence.room.Entity
import java.math.BigDecimal

@Entity(tableName = "contracts")
data class ContractData(
        val downloadspending: BigDecimal,
        val endheight: Int,
        val fees: BigDecimal,
        val id: String,
        val netaddress: String,
        val renterfunds: BigDecimal,
        val size: BigDecimal,
        val startheight: Int,
        val storagespending: BigDecimal,
        val totalcost: BigDecimal,
        val uploadspending: BigDecimal,
        val goodforupload: Boolean,
        val goodforrenew: Boolean)