/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "wallet")
data class WalletData(
        val encrypted: Boolean,
        val unlocked: Boolean,
        val rescanning: Boolean,
        val confirmedsiacoinbalance: BigDecimal,
        val unconfirmedoutgoingsiacoins: BigDecimal,
        val unconfirmedincomingsiacoins: BigDecimal,
        val siafundbalance: Int,
        val siacoinclaimbalance: BigDecimal,
        val dustthreshold: BigDecimal) {

    @Transient
    var unconfirmedsiacoinbalance: BigDecimal = unconfirmedincomingsiacoins - unconfirmedoutgoingsiacoins

    @Transient
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    var timestamp = System.currentTimeMillis()
}