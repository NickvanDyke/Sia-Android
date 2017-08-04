package vandyke.siamobile.backend.coldstorage

import java.math.BigDecimal

data class SiacoinOutputModel(val value: BigDecimal = BigDecimal.ZERO,
                              val unlockhash: String = "")