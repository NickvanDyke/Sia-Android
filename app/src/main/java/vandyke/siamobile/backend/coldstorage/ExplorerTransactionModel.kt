package vandyke.siamobile.backend.coldstorage

import java.math.BigDecimal

data class ExplorerTransactionModel(val id: String,
                                    val height: Long,
                                    val rawtransaction: RawTransactionModel,
                                    val siacoininputoutputs: ArrayList<SiacoinOutputModel>) {

    val value: BigDecimal by lazy {
        var total = BigDecimal.ZERO
        for (output in rawtransaction.siacoinoutputs) // TODO: might have to check if the address is from the wallet or not too
            total += output.value
        for (output in siacoininputoutputs)
            total -= output.value
        total
    }
}