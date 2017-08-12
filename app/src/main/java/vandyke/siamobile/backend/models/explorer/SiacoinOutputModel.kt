/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.models.explorer

import java.math.BigDecimal

data class SiacoinOutputModel(val value: BigDecimal = BigDecimal.ZERO,
                              val unlockhash: String = "")