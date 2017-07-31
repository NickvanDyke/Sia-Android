package vandyke.siamobile.backend.wallet.transaction

data class KtTransaction(val transactionid: String,
                         val confirmationheight: Long,
                         val confirmationtimestamp: Long)