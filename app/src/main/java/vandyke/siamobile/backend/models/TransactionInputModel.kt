package vandyke.siamobile.backend.models

import java.math.BigDecimal

data class TransactionInputModel(val parentid: String = "",
                                 val fundtype: String = "",
                                 val walletaddress: Boolean = false,
                                 val relatedaddress: String = "",
                                 val value: BigDecimal = BigDecimal.ZERO)