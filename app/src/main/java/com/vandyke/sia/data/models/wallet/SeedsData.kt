/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.wallet

data class SeedsData(
        val primaryseed: String,
        val addressesremaining: Int,
        val allseeds: List<String>)