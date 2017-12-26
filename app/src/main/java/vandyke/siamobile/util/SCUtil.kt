/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.util

import vandyke.siamobile.data.local.Prefs
import java.math.BigDecimal

val HASTINGS_PER_SC = BigDecimal("1000000000000000000000000")

val BLOCK_100k_TIMESTAMP = 1492126789

val UNCONFIRMED_TX_TIMESTAMP = "18446744073709551615"

fun BigDecimal.toSC(): BigDecimal = divide(HASTINGS_PER_SC)

fun String.toSC(): BigDecimal = if (this.isBlank()) BigDecimal.ZERO else BigDecimal(this).divide(HASTINGS_PER_SC)

fun BigDecimal.toHastings(): BigDecimal = multiply(HASTINGS_PER_SC)

fun String.toHastings(): BigDecimal = if (this.isBlank()) BigDecimal.ZERO else BigDecimal(this).multiply(HASTINGS_PER_SC)

fun BigDecimal.round(): BigDecimal = setScale(Prefs.displayedDecimalPrecision, BigDecimal.ROUND_CEILING)

fun BigDecimal.toUsd(usdPrice: BigDecimal): BigDecimal = multiply(usdPrice)

object SCUtil {
    fun estimatedBlockHeightAt(time: Long): Long {
        val blockTime = 9 // overestimate
        val diff = time - BLOCK_100k_TIMESTAMP
        return (100000 + diff / 60 / blockTime.toLong())
    }

    fun estimatedTimeAtBlock(height: Long): Long {
        val blockTime = 10
        val heightDiff = height - 100000
        val timeDiff = heightDiff * blockTime
        return (BLOCK_100k_TIMESTAMP + timeDiff * 60)
    }
}
