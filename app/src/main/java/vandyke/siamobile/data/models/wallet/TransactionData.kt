/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.models.wallet

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import vandyke.siamobile.util.UNCONFIRMED_TX_TIMESTAMP
import java.math.BigDecimal
import java.util.*

@Entity(tableName = "transactions")
@JsonIgnoreProperties(ignoreUnknown = true)
data class TransactionData @JsonCreator constructor(
        @PrimaryKey
        @JsonProperty(value = "transactionid")
        val transactionId: String,
        @JsonProperty(value = "confirmationheight")
        val confirmationHeight: BigDecimal,
        @JsonProperty(value = "confirmationtimestamp")
        val confirmationTimestamp: BigDecimal,
        @Ignore
        @JsonProperty(value = "inputs")
        val inputs: List<TransactionInputData>? = null,
        @Ignore
        @JsonProperty(value = "outputs")
        val outputs: List<TransactionOutputData>? = null) {


    constructor(transactionId: String,
                confirmationHeight: BigDecimal,
                confirmationTimestamp: BigDecimal) :
            this(transactionId, confirmationHeight, confirmationTimestamp, null, null)

    var confirmed: Boolean = confirmationTimestamp != BigDecimal(UNCONFIRMED_TX_TIMESTAMP)

    var netValue: BigDecimal = run {
        var net = BigDecimal.ZERO
        inputs?.filter { it.walletaddress }?.forEach { net -= it.value }
        outputs?.filter { it.walletaddress }?.forEach { net += it.value }
        net
    }

    val isNetZero: Boolean
        get() = netValue == BigDecimal.ZERO

    @Ignore
    val confirmationDate: Date = if (confirmed) Date((confirmationTimestamp * BigDecimal("1000")).toLong()) else Date()
}