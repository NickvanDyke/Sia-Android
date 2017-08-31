/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.data.wallet

data class SeedsData(val primaryseed: String = "",
                     val addressesremaining: Int = 0,
                     val allseeds: ArrayList<String> = ArrayList())