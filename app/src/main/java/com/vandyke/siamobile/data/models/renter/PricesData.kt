/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.data.models.renter

import java.math.BigDecimal

data class PricesData(val downloadterabyte: BigDecimal = BigDecimal.ZERO,
                      val formcontracts: BigDecimal = BigDecimal.ZERO,
                      val storageterabytemonth: BigDecimal = BigDecimal.ZERO,
                      val uploadterabyte: BigDecimal = BigDecimal.ZERO)