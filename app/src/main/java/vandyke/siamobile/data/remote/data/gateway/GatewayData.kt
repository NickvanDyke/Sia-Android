/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.remote.data.gateway

data class GatewayData(val netaddress: String = "",
                       val peers: List<GatewayPeerData> = listOf())