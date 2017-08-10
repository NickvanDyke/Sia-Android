package vandyke.siamobile.backend.models.wallet

import java.math.BigDecimal

data class TransactionOutputModel(val id: String = "",
                                  val fundtype: String = "",
                                  val maturityheight: BigDecimal = BigDecimal.ZERO,
                                  val walletaddress: Boolean = false,
                                  val relatedaddress: String = "",
                                  val value: BigDecimal = BigDecimal.ZERO)