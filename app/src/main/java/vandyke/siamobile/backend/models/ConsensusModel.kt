package vandyke.siamobile.backend.models

import vandyke.siamobile.util.SCUtil

data class ConsensusModel(val synced: Boolean = false,
                          val height: Long = 0,
                          val currentblock: String = "",
                          val difficulty: String = "") {
    val syncprogress: Double by lazy { height.toDouble() / SCUtil.estimatedBlockHeightAt(System.currentTimeMillis() / 1000) * 100 }
}