/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.gateway

data class GatewayPeerData(val netaddress: String,
                           val version: String,
                           val inbound: Boolean)