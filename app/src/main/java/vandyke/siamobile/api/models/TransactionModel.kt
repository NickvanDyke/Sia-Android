package vandyke.siamobile.api.models

import java.math.BigDecimal
import java.util.*

data class TransactionModel(val transactionid: String = "",
                            val confirmationheight: Long = 0,
                            val confirmationtimestamp: Long = 0,
                            val inputs: ArrayList<TransactionInputModel> = ArrayList(),
                            val outputs: ArrayList<TransactionOutputModel> = ArrayList()) {

    val confirmed: Boolean by lazy { confirmationtimestamp != 9223372036854775807 }

    val netValue: BigDecimal by lazy {
        var net = BigDecimal.ZERO
        for (input in inputs)
            if (input.walletaddress)
                net -= input.value
        for (output in outputs)
            if (output.walletaddress)
                net += output.value
        net
    }

    val isNetZero: Boolean by lazy { netValue == BigDecimal.ZERO }

    val confirmationDate: Date by lazy { if (confirmed) Date(confirmationtimestamp * 1000) else Date() }
}