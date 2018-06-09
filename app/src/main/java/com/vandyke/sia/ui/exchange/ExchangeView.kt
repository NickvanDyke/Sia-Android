package com.vandyke.sia.ui.exchange

import com.hannesdorfmann.mosby3.mvp.MvpView
import io.reactivex.Observable
import java.math.BigDecimal

interface ExchangeView : MvpView {
    fun fromAmount(): Observable<BigDecimal>

    fun fromCoin(): Observable<String>

    fun toAmount(): Observable<BigDecimal>

    fun toCoin(): Observable<String>

    fun render(state: ExchangeViewState)
}