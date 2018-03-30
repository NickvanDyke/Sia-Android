package com.vandyke.sia.util

import com.vandyke.sia.data.local.Prefs
import java.text.NumberFormat

fun Int.format(): String {
    val nf = NumberFormat.getInstance()
    nf.maximumFractionDigits = Prefs.displayedDecimalPrecision
    return nf.format(this)
}

fun Long.format(): String {
    val nf = NumberFormat.getInstance()
    nf.maximumFractionDigits = Prefs.displayedDecimalPrecision
    return nf.format(this)
}

fun Float.format() : String {
    val nf = NumberFormat.getInstance()
    nf.maximumFractionDigits = Prefs.displayedDecimalPrecision
    return nf.format(this)
}

fun Double.format(): String {
    val nf = NumberFormat.getInstance()
    nf.maximumFractionDigits = Prefs.displayedDecimalPrecision
    return nf.format(this)
}