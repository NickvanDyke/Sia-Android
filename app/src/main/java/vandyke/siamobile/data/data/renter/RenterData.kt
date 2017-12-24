/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.data.renter

data class RenterData(val settings: RenterSettingsData = RenterSettingsData(),
                      val financialmetrics: RenterFinancialMetricsData = RenterFinancialMetricsData(),
                      val currentperiod: Int = 0)