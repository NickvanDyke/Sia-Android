/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.remote.SiaApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GatewayRepository
@Inject constructor(
        private val api: SiaApi
) {
    fun getGateway() = api.gateway()
}