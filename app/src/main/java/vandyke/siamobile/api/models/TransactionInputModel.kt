package vandyke.siamobile.api.models

import java.math.BigDecimal

data class TransactionInputModel(val parentid: String,
                                 val fundtype: String,
                                 val walletaddress: Boolean,
                                 val relatedaddress: String,
                                 val value: BigDecimal)