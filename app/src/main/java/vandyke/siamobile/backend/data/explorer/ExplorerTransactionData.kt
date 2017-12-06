/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.data.explorer

data class ExplorerTransactionData(val id: String = "",
                                   val height: Int = 0,
                                   val rawtransaction: RawTransactionData = RawTransactionData(),
                                   val siacoininputoutputs: List<SiacoinOutputData> = listOf())