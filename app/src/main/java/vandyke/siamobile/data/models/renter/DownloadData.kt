/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.models.renter

import java.math.BigDecimal

data class DownloadData(val siapath: String = "",
                        val destination: String = "",
                        val filesize: BigDecimal = BigDecimal.ZERO,
                        val received: BigDecimal = BigDecimal.ZERO,
                        val starttime: String = "",
                        val error: String = "")