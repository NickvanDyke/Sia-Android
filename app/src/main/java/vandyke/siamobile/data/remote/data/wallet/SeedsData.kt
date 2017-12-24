/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.remote.data.wallet

data class SeedsData(val primaryseed: String = "",
                     val addressesremaining: Int = 0,
                     val allseeds: List<String> = listOf())