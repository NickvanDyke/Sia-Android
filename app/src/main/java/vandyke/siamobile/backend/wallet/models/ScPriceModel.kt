/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.wallet.models

import java.math.BigDecimal

data class ScPriceModel(val price_usd: BigDecimal = BigDecimal.ZERO)