package vandyke.siamobile.data.data.renter

data class RenterData(val settings: RenterSettingsData = RenterSettingsData(),
                      val financialmetrics: RenterFinancialMetricsData = RenterFinancialMetricsData(),
                      val currentperiod: Int = 0)