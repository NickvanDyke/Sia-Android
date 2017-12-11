package vandyke.siamobile.data.data.renter

import vandyke.siamobile.data.data.wallet.TransactionData
import java.math.BigDecimal

data class ContractData(val endheight: Int = 0,
                        val id: String = "",
                        val netaddress: String = "",
                        val lasttransaction: TransactionData = TransactionData(),
                        val renterfunds: BigDecimal = BigDecimal.ZERO,
                        val size: BigDecimal = BigDecimal.ZERO)