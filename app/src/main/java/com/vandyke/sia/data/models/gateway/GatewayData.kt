/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.gateway

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

// TODO: without this ignore annotation, Jackson claims that netaddress on the JSON it's deserializing is an
// unknown field. Not sure why. I think it's due to minifying, because it was fine before.
// Anyway, the field will be blank upon deserialization. Right now that doesn't matter, but it might eventually.
@JsonIgnoreProperties(ignoreUnknown = true)
data class GatewayData(val netaddress: String = "",
                       val peers: List<GatewayPeerData> = listOf())