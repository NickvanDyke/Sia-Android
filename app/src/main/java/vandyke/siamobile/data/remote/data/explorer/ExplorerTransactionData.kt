/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.data.remote.data.explorer

data class ExplorerTransactionData(val id: String = "",
                                   val height: Int = 0,
                                   val rawtransaction: RawTransactionData = RawTransactionData(),
                                   val siacoininputoutputs: List<SiacoinOutputData> = listOf())