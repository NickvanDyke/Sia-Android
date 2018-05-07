package com.vandyke.sia.data.repository

import com.vandyke.sia.data.remote.ShapeShiftApi
import com.vandyke.sia.data.remote.SiaApi
import javax.inject.Inject

class ExchangeRepository
@Inject constructor(
        private val shapeShiftApi: ShapeShiftApi,
        private val siaApi: SiaApi
) {
    fun getMarketInfo(from: String, to: String) = shapeShiftApi.getMarketInfo("${from}_$to")
}