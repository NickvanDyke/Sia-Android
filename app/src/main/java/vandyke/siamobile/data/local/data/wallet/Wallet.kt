/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local.data.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import vandyke.siamobile.data.remote.data.wallet.WalletData
import java.math.BigDecimal

@Entity(tableName = "wallet")
data class Wallet(@PrimaryKey
                  val timestamp: Long,
                  val encrypted: Boolean,
                  val unlocked: Boolean,
                  val rescanning: Boolean,
                  val confirmedHastings: BigDecimal,
                  val unconfirmedHastings: BigDecimal,
                  val siafunds: Int) {

    companion object {
        fun fromWalletData(it: WalletData) = Wallet(System.currentTimeMillis(),
                it.encrypted,
                it.unlocked,
                it.rescanning,
                it.confirmedsiacoinbalance,
                it.unconfirmedsiacoinbalance,
                it.siafundbalance)
    }
}