/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.models.gateway

data class GatewayData(val netaddress: String = "",
                       val peers: List<GatewayPeerData> = listOf())