package vandyke.siamobile.backend.data.renter

import vandyke.siamobile.backend.data.wallet.TransactionData
import java.math.BigDecimal

data class ContractData(val endheight: Int = 0,
                        val id: String = "",
                        val netaddress: String = "",
                        val lasttransaction: TransactionData = TransactionData(),
                        val renterfunds: BigDecimal = BigDecimal.ZERO,
                        val size: BigDecimal = BigDecimal.ZERO)