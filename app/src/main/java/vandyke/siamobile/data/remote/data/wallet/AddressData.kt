/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.remote.data.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

@Entity(tableName = "addresses")
data class AddressData @JsonCreator constructor(@PrimaryKey
                                                @JsonProperty(value = "address")
                                                val address: String)