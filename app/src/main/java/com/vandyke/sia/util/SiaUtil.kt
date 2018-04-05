/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.util

import android.os.Build
import com.vandyke.sia.data.local.Prefs
import java.math.BigDecimal
import java.text.NumberFormat

const val BLOCKTIME = 600 // seconds

val HASTINGS_PER_SC = BigDecimal("1000000000000000000000000")

const val BLOCK_139949_TIMESTAMP = 1517519551 // seconds (Unix timestamp)

val UNCONFIRMED_TX_TIMESTAMP = BigDecimal("18446744073709551615") // need to use BigDecimal because it's out of range of Long

fun BigDecimal.toSC(): BigDecimal = this.divide(HASTINGS_PER_SC)

fun String.toSC(): BigDecimal = if (this.isBlank()) BigDecimal.ZERO else BigDecimal(this).divide(HASTINGS_PER_SC)

fun BigDecimal.toHastings(): BigDecimal = this.multiply(HASTINGS_PER_SC)

fun String.toHastings(): BigDecimal = if (this.isBlank()) BigDecimal.ZERO else BigDecimal(this).multiply(HASTINGS_PER_SC)

fun BigDecimal.format(): String {
    val nf = NumberFormat.getInstance()
    nf.maximumFractionDigits = Prefs.displayedDecimalPrecision
    return nf.format(this)
}

object SiaUtil {
    /** @param time timestamp, in seconds */
    fun estimatedBlockHeightAt(time: Long): Long {
        val blockTime = 9 // overestimate
        val diff = time - BLOCK_139949_TIMESTAMP
        return (139949 + diff / 60 / blockTime.toLong())
    }

    /** @return Estimated Unix timestamp (seconds) at given block height */
    fun estimatedTimeAtBlock(height: Long): Long {
        val blockTime = 10
        val heightDiff = height - 139949
        val timeDiff = heightDiff * blockTime
        return (BLOCK_139949_TIMESTAMP + timeDiff * 60)
    }

    fun blockHeightToReadableTimeDiff(height: Long): String {
        var time = estimatedTimeAtBlock(height) - System.currentTimeMillis() / 1000
        var result = ""
        if (time < 0) {
            result += "-"
            time = Math.abs(time)
        }
        if (time > 86400) {
            val divisor = time / 86400
            time -= 86400 * divisor
            result += "${divisor}d"
        }
        if (time > 3600) {
            if (result.isNotEmpty())
                result += " "
            val divisor = time / 3600
            time -= 3600 * divisor
            result += "${divisor}h"
        }
        if (time > 60) {
            if (result.isNotEmpty())
                result += " "
            val divisor = time / 3600
            time -= 60 * divisor
            result += "${divisor}m"
        }
        if (time > 60) {
            if (result.isNotEmpty())
                result += " "
            result += "${time}ms"
        }
        return result
    }

    fun blocksToDays(blocks: Int) = (blocks * BLOCKTIME).toFloat() / (24 * 60 * 60)

    fun daysToBlocks(days: Double) = (days * 24 * 60 * 60) / BLOCKTIME

    val isSiadSupported = Build.SUPPORTED_64_BIT_ABIS.any { it == "arm64-v8a" }
}
