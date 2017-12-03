package vandyke.siamobile.backend.data.renter

import java.math.BigDecimal

data class DownloadData(val siapath: String = "",
                        val destination: String = "",
                        val filesize: BigDecimal = BigDecimal.ZERO,
                        val received: BigDecimal = BigDecimal.ZERO,
                        val starttime: String = "",
                        val error: String = "")