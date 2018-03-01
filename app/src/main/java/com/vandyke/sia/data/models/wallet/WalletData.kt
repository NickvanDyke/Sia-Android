/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json
import java.math.BigDecimal

@Entity(tableName = "wallet")
data class WalletData(
        @Json(name = "encrypted")
        val encrypted: Boolean = false,
        @Json(name = "unlocked")
        val unlocked: Boolean = false,
        @Json(name = "rescanning")
        val rescanning: Boolean = false,
        @Json(name = "confirmedsiacoinbalance")
        val confirmedSiacoinBalance: BigDecimal,
        @Json(name = "unconfirmedoutgoingsiacoins")
        val unconfirmedOutgoingSiacoins: BigDecimal,
        @Json(name = "unconfirmedincomingsiacoins")
        val unconfirmedIncomingSiacoins: BigDecimal,
        @Json(name = "siafundbalance")
        val siafundBalance: Int = 0,
        @Json(name = "siacoinclaimbalance")
        val siacoinClaimBalance: BigDecimal,
        @Json(name = "dustthreshold")
        val dustThreshold: BigDecimal) {

    @Transient
    var unconfirmedSiacoinBalance: BigDecimal = unconfirmedIncomingSiacoins - unconfirmedOutgoingSiacoins

    @Transient
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    var timestamp = System.currentTimeMillis()
}