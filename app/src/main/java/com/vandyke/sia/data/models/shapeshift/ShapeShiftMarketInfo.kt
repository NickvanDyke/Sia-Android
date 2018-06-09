package com.vandyke.sia.data.models.shapeshift

import java.math.BigDecimal

data class ShapeShiftMarketInfo(
        val pair: String,
        val rate: BigDecimal,
        val limit: BigDecimal,
        val minimum: BigDecimal,
        val minerFee: BigDecimal
)