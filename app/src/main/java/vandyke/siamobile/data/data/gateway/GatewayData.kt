package vandyke.siamobile.data.data.gateway

data class GatewayData(val netaddress: String = "",
                       val peers: List<GatewayPeerData> = listOf())