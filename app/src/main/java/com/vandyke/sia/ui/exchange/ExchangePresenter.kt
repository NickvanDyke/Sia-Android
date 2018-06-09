package com.vandyke.sia.ui.exchange

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.vandyke.sia.data.models.shapeshift.ShapeShiftMarketInfo
import com.vandyke.sia.data.remote.ShapeShiftApi
import com.vandyke.sia.data.repository.ExchangeRepository
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import javax.inject.Inject

class ExchangePresenter
@Inject constructor(
    private val exchangeRepository: ExchangeRepository
) : MviBasePresenter<ExchangeView, ExchangeViewState>() {
    override fun bindIntents() {

        val availableCoins = exchangeRepository.getAvailableCoins()
                .toObservable()
                .map { PartialChange.AvailableCoinsUpdate(it) }

        val enteringAmount = Observable.merge(
                intent(ExchangeView::fromAmount)
                        .map { PartialChange.TypedFromAmountUpate(it) },
                intent(ExchangeView::toAmount)
                        .map { PartialChange.TypedToAmountUpate(it) })

        val pairInfo = Observable.combineLatest(
                intent(ExchangeView::fromCoin),
                intent(ExchangeView::toCoin),
                BiFunction { fromCoin: String, toCoin: String -> fromCoin to toCoin })
                .distinctUntilChanged()
                .switchMap { (from, to) ->
                    exchangeRepository.getMarketInfo(from, to)
                            .map<PartialChange> { PartialChange.MarketInfoUpdate(it) }
                            .toObservable()
                            .startWith(PartialChange.LoadingMarketInfo())
                }

        val initialState = ExchangeViewState(
                ShapeShiftApi.currencies,
                ShapeShiftMarketInfo("", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                "BTC",
                "SC",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "",
                false
        )

        val allIntents = Observable.mergeArray(availableCoins, enteringAmount, pairInfo)

        subscribeViewState(
                allIntents.scan(initialState, this::reducer)
                        .distinctUntilChanged(),
                ExchangeView::render)
    }

    private fun reducer(state: ExchangeViewState, change: PartialChange): ExchangeViewState {
        return with(state) {
            when (change) {
                is PartialChange.AvailableCoinsUpdate -> copy(coins = change.coins)
                is PartialChange.MarketInfoUpdate -> copy(pairInfo = change.info, loading = false)
                is PartialChange.LoadingMarketInfo -> copy(loading = true)
                is PartialChange.TypedFromAmountUpate -> copy(toAmount = change.amount * state.pairInfo.rate)
                is PartialChange.TypedToAmountUpate -> copy(fromAmount = change.amount / state.pairInfo.rate)
            }
        }
    }
}