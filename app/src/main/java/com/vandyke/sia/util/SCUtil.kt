/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import com.vandyke.sia.data.local.Prefs
import java.math.BigDecimal
import java.text.NumberFormat

val HASTINGS_PER_SC = BigDecimal("1000000000000000000000000")

const val BLOCK_139949_TIMESTAMP = 1517519551

val UNCONFIRMED_TX_TIMESTAMP = BigDecimal("18446744073709551615")

fun BigDecimal.toSC(): BigDecimal = this.divide(HASTINGS_PER_SC)

fun String.toSC(): BigDecimal = if (this.isBlank()) BigDecimal.ZERO else BigDecimal(this).divide(HASTINGS_PER_SC)

fun BigDecimal.toHastings(): BigDecimal = this.multiply(HASTINGS_PER_SC)

fun String.toHastings(): BigDecimal = if (this.isBlank()) BigDecimal.ZERO else BigDecimal(this).multiply(HASTINGS_PER_SC)

fun BigDecimal.toUsd(usdPrice: BigDecimal): BigDecimal = multiply(usdPrice)

fun BigDecimal.format(): String {
    val nf = NumberFormat.getNumberInstance()
    nf.maximumFractionDigits = Prefs.displayedDecimalPrecision
    return nf.format(this)
}

object SCUtil {
    fun estimatedBlockHeightAt(time: Long): Long {
        val blockTime = 9 // overestimate
        val diff = time - BLOCK_139949_TIMESTAMP
        return (139949 + diff / 60 / blockTime.toLong())
    }

    fun estimatedTimeAtBlock(height: Long): Long {
        val blockTime = 10
        val heightDiff = height - 100000
        val timeDiff = heightDiff * blockTime
        return (BLOCK_139949_TIMESTAMP + timeDiff * 60)
    }
}
