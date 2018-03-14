/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import java.math.BigDecimal

data class DownloadData(
        val siapath: String,
        val destination: String,
        val filesize: BigDecimal,
        val received: BigDecimal,
        val starttime: String,
        val error: String)