package vandyke.siamobile.backend.coldstorage

import java.math.BigDecimal

data class ExplorerTransactionModel(val id: String = "",
                                    val height: Long = 0,
                                    val rawtransaction: RawTransactionModel = RawTransactionModel(),
                                    val siacoininputoutputs: ArrayList<SiacoinOutputModel> = ArrayList()) {

    val value: BigDecimal by lazy {
        var total = BigDecimal.ZERO
        for (output in rawtransaction.siacoinoutputs)
            total += output.value
        for (output in siacoininputoutputs)
            total -= output.value
        total
    }
}