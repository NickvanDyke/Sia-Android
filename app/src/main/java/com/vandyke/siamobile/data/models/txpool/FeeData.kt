/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.models.txpool

import java.math.BigDecimal

data class FeeData(val minimum: BigDecimal,
                   val maximum: BigDecimal)