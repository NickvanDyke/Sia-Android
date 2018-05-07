package com.vandyke.sia.data.repository

import com.vandyke.sia.data.remote.SiaApi
import javax.inject.Inject

class ExchangeRepository
@Inject constructor(
        private val api: SiaApi
) {

}