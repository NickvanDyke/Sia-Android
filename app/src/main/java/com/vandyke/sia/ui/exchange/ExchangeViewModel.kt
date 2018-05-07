package com.vandyke.sia.ui.exchange

import android.arch.lifecycle.ViewModel
import com.vandyke.sia.data.repository.ExchangeRepository
import javax.inject.Inject

class ExchangeViewModel
@Inject constructor(
        private val exchangeRepository: ExchangeRepository
) : ViewModel() {

}