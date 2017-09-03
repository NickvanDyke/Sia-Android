/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.data.renter

import java.math.BigDecimal

abstract class SiaNode {
    abstract val parent: SiaDir?
    abstract val name: String
    abstract val size: BigDecimal
}