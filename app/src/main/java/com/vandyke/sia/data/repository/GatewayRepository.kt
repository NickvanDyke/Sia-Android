/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.repository

import com.vandyke.sia.data.remote.SiaApiInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GatewayRepository
@Inject constructor(
        private val api: SiaApiInterface
) {
    fun getGateway() = api.gateway()
}