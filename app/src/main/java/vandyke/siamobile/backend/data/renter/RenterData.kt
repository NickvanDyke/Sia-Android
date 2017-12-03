package vandyke.siamobile.backend.data.renter

data class RenterData(val settings: RenterSettingsData = RenterSettingsData(),
                      val financialmetrics: RenterFinancialMetrics = RenterFinancialMetrics(),
                      val currentperiod: Int = 0)