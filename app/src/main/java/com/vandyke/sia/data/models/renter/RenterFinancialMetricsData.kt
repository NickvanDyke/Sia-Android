/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

import java.math.BigDecimal

data class RenterFinancialMetricsData(val contractspending: BigDecimal = BigDecimal.ZERO,
                                      val downloadspending: BigDecimal = BigDecimal.ZERO,
                                      val storagespending: BigDecimal = BigDecimal.ZERO,
                                      val uploadspending: BigDecimal = BigDecimal.ZERO,
                                      val unspent: BigDecimal = BigDecimal.ZERO)