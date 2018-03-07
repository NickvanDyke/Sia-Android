/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.node.modules

import java.math.BigDecimal

data class ModuleData(
        val name: String,
        val on: Boolean,
        val size: BigDecimal
)