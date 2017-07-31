package vandyke.siamobile.api.models

data class TransactionsModel(val confirmedtransactions: ArrayList<TransactionModel>? = ArrayList(),
                             val unconfirmedtransactions: ArrayList<TransactionModel>? = ArrayList())