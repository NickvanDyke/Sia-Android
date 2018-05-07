package com.vandyke.sia.ui.exchange

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.models.shapeshift.ShapeShiftMarketInfo
import com.vandyke.sia.data.remote.ShapeShiftApi
import com.vandyke.sia.data.repository.ExchangeRepository
import com.vandyke.sia.util.rx.*
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import javax.inject.Inject

class ExchangeViewModel
@Inject constructor(
        private val exchangeRepository: ExchangeRepository
) : ViewModel() {
    val coins = MutableNonNullLiveData(ShapeShiftApi.currencies)
    val from = MutableNonNullLiveData("BTC")
    val fromAmount = MutableNonNullLiveData(BigDecimal.ZERO)
    val to = MutableNonNullLiveData("SC")
    val toAmount = MutableNonNullLiveData(BigDecimal.ZERO)
    val marketInfo = MutableLiveData<ShapeShiftMarketInfo>()

    val activeTasks = MutableNonNullLiveData(0)
    val refreshing = MutableNonNullLiveData(false)
    val error = MutableSingleLiveEvent<Throwable>()

    private var settingFrom = false
    private var settingTo = false

    init {
        Flowable.combineLatest(
                from.toFlowable(),
                to.toFlowable(),
                BiFunction { from: String, to: String -> from to to })
                .flatMapSingle {
                    exchangeRepository.getMarketInfo(it.first, it.second)
                            .io()
                            .main()
                            .track(activeTasks)
                }
                .subscribe(marketInfo::setValue, error::setValue)

        fromAmount.observeForevs {
            if (settingFrom)
                return@observeForevs
            settingTo = true
            toAmount.value = it * (marketInfo.value?.rate ?: return@observeForevs)
            settingTo = false
        }

        toAmount.observeForevs {
            if (settingTo)
                return@observeForevs
            settingFrom = true
            fromAmount.value = it / (marketInfo.value?.rate ?: return@observeForevs)
            settingFrom = false
        }

        marketInfo.observeForevs {
            settingTo = true
            toAmount.value = fromAmount.value * it.rate
            settingTo = false
        }

//        refreshMarketInfo()
    }

    fun refreshMarketInfo() {
        exchangeRepository.getMarketInfo(from.value, to.value)
                .io()
                .main()
                .track(refreshing)
                .subscribe(marketInfo::setValue, error::setValue)
    }


}