/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.remote.data.renter

import java.math.BigDecimal

data class RenterSettingsAllowanceData(val funds: BigDecimal = BigDecimal.ZERO,
                                       val hosts: Int = 0,
                                       val period: Int = 0,
                                       val renewwindow: Int = 0)