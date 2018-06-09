package com.vandyke.sia.ui.exchange

import com.vandyke.sia.data.models.shapeshift.ShapeShiftMarketInfo
import java.math.BigDecimal

sealed class PartialChange {
    class LoadingMarketInfo : PartialChange()
    data class MarketInfoUpdate(val info: ShapeShiftMarketInfo) : PartialChange()
    data class AvailableCoinsUpdate(val coins: List<String>) : PartialChange()
    data class TypedFromAmountUpate(val amount: BigDecimal) : PartialChange()
    data class TypedToAmountUpate(val amount: BigDecimal) : PartialChange()
}