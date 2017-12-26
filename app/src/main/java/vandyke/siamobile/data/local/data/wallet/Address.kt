/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.local.data.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import vandyke.siamobile.data.remote.data.wallet.AddressData
import vandyke.siamobile.data.remote.data.wallet.AddressesData

@Entity(tableName = "addresses")
data class Address(@PrimaryKey val address: String) {
    companion object {
        fun fromAddressData(it: AddressData) = Address(it.address)

        fun fromAddressesData(it: AddressesData) = it.addresses.map { Address(it) }
    }
}