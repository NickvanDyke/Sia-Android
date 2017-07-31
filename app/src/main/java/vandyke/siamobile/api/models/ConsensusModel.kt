package vandyke.siamobile.api.models

data class ConsensusModel(val synced: Boolean = false,
                          val height: Long = 0,
                          val currentblock: String = "",
                          val difficulty: String = "") {
    val syncprogress: Double by lazy { height.toDouble() / estimatedBlockHeightAt(System.currentTimeMillis() / 1000) * 100 }

    fun estimatedBlockHeightAt(time: Long): Int {
        val block100kTimestamp: Long = 1492126789 // Unix timestamp; seconds
        val blockTime = 9 // overestimate
        val diff = time - block100kTimestamp
        return (100000 + diff / 60 / blockTime.toLong()).toInt()
    }
}