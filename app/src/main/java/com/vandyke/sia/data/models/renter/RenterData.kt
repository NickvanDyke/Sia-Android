/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.models.renter

data class RenterData(val settings: RenterSettingsData = RenterSettingsData(),
                      val financialmetrics: RenterFinancialMetricsData = RenterFinancialMetricsData(),
                      val currentperiod: Int = 0)