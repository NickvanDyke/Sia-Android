package vandyke.siamobile.util

import vandyke.siamobile.prefs
import java.math.BigDecimal

fun BigDecimal.toSC(): BigDecimal = divide(BigDecimal("1000000000000000000000000"))

fun String.toSC(): BigDecimal = BigDecimal(this).divide(BigDecimal("1000000000000000000000000"))

fun BigDecimal.round():BigDecimal = setScale(prefs.displayedDecimalPrecision, BigDecimal.ROUND_CEILING)
