/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.data.consensus

import vandyke.siamobile.util.SCUtil

data class ConsensusData(val synced: Boolean = false,
                         val height: Int = 0,
                         val currentblock: String = "",
                         val difficulty: Long = 0) {
    val syncprogress: Double by lazy { height.toDouble() / SCUtil.estimatedBlockHeightAt(System.currentTimeMillis() / 1000) * 100 }
}