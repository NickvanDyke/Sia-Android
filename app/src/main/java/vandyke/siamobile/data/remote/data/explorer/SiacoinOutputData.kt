/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.remote.data.explorer

import java.math.BigDecimal

data class SiacoinOutputData(val value: BigDecimal = BigDecimal.ZERO,
                             val unlockhash: String = "")