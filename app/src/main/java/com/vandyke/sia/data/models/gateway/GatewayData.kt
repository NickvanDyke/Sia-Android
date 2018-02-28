/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.gateway

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

// TODO: without this ignore annotation, Jackson claims that netaddress on the JSON it's deserializing is an
// unknown field. Not sure why. I think it's due to minifying, because it was fine before.
// Anyway, the field will be blank upon deserialization. Right now that doesn't matter, but it might eventually.
data class GatewayData
@JsonCreator constructor(
        @JsonProperty(value = "netaddress")
        val netaddress: String,
        @JsonProperty(value = "peers")
        val peers: List<GatewayPeerData>)