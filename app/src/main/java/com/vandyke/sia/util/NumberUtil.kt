package com.vandyke.sia.util

import com.vandyke.sia.data.local.Prefs
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

fun Int.format(): String {
    return customNumberFormat.format(this)
}

fun Long.format(): String {
    return customNumberFormat.format(this)
}

fun Float.format(): String {
    return customNumberFormat.format(this)
}

fun Double.format(): String {
    return customNumberFormat.format(this)
}

fun BigDecimal.format(): String {
    return customNumberFormat.format(this)
}

private val customNumberFormat: NumberFormat
    get() {
        val nf = NumberFormat.getInstance()
        nf.maximumFractionDigits = Prefs.displayedDecimalPrecision
        nf.roundingMode = RoundingMode.UP
        return nf
    }