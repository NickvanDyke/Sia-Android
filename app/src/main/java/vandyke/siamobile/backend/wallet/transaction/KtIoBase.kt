package vandyke.siamobile.backend.wallet.transaction

import java.math.BigDecimal

open class KtIoBase(val fundtype: String, val walletaddress: Boolean, val relatedaddress: String, val value: BigDecimal)