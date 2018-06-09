package com.vandyke.sia.ui.exchange

import com.vandyke.sia.data.models.shapeshift.ShapeShiftMarketInfo
import java.math.BigDecimal

data class ExchangeViewState(val coins: List<String>,
                             val pairInfo: ShapeShiftMarketInfo,
                             val fromCoin: String,
                             val toCoin: String,
                             val fromAmount: BigDecimal,
                             val toAmount: BigDecimal,
                             val receiveAddress: String,
                             val loading: Boolean)