/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.remote.data.gateway

data class GatewayPeerData(val netaddress: String = "",
                           val version: String = "",
                           val inbound: Boolean = false)