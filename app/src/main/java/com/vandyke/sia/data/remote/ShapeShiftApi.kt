package com.vandyke.sia.data.remote

import com.vandyke.sia.data.models.shapeshift.ShapeShiftMarketInfo
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path


interface ShapeShiftApi {
    @GET("marketinfo/{pair}")
    fun getMarketInfo(@Path("pair") pair: String): Single<ShapeShiftMarketInfo>

    companion object {
        /* ShapeShift API endpoint for getting supported coins is really bad, so we just have them here */
        val currencies = listOf("BTC", "LTC", "SC")
    }
}