/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.models.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@Entity(tableName = "wallet")
data class WalletData @JsonCreator constructor(
        @JsonProperty(value = "encrypted")
        val encrypted: Boolean = false,
        @JsonProperty(value = "unlocked")
        val unlocked: Boolean = false,
        @JsonProperty(value = "rescanning")
        val rescanning: Boolean = false,
        @JsonProperty(value = "confirmedsiacoinbalance")
        val confirmedSiacoinBalance: BigDecimal,
        @JsonProperty(value = "unconfirmedoutgoingsiacoins")
        val unconfirmedOutgoingSiacoins: BigDecimal,
        @JsonProperty(value = "unconfirmedincomingsiacoins")
        val unconfirmedIncomingSiacoins: BigDecimal,
        @JsonProperty(value = "siafundbalance")
        val siafundBalance: Int = 0,
        @JsonProperty(value = "siacoinclaimbalance")
        val siacoinClaimBalance: BigDecimal,
        @JsonProperty(value = "dustthreshold")
        val dustThreshold: BigDecimal) {

    @JsonIgnore(value = true)
    var unconfirmedSiacoinBalance: BigDecimal = unconfirmedIncomingSiacoins - unconfirmedOutgoingSiacoins

    @PrimaryKey
    @JsonIgnore(value = true)
    var timestamp = System.currentTimeMillis()
}