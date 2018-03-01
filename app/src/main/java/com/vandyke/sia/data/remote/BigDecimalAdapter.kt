/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal

class BigDecimalAdapter {
    @ToJson
    fun toJson(bd: BigDecimal): String = bd.toPlainString()

    @FromJson
    fun fromJson(string: String): BigDecimal = BigDecimal(string)
}