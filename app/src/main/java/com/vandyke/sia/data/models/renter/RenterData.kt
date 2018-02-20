/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

data class RenterData(val settings: RenterSettingsData,
                      val financialmetrics: RenterFinancialMetricsData,
                      val currentperiod: Int = 0)