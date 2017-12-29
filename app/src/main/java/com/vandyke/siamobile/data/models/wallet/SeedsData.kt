/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.models.wallet

data class SeedsData(val primaryseed: String = "",
                     val addressesremaining: Int = 0,
                     val allseeds: List<String> = listOf())