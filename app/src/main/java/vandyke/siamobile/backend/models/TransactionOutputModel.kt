package vandyke.siamobile.backend.models

import java.math.BigDecimal

data class TransactionOutputModel(val id: String,
                                  val fundtype: String,
                                  val maturityheight: Long,
                                  val walletaddress: Boolean,
                                  val relatedaddress: String,
                                  val value: BigDecimal)