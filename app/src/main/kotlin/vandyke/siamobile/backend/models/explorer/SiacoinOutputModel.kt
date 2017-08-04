package vandyke.siamobile.backend.models.explorer

import java.math.BigDecimal

data class SiacoinOutputModel(val value: BigDecimal = BigDecimal.ZERO,
                              val unlockhash: String = "")