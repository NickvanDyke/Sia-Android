/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet

import vandyke.siamobile.backend.data.consensus.ConsensusData
import vandyke.siamobile.backend.data.wallet.ScPriceData
import vandyke.siamobile.backend.data.wallet.TransactionsData
import vandyke.siamobile.backend.data.wallet.WalletData
import vandyke.siamobile.backend.networking.SiaError

interface IWalletView {
    fun onWalletUpdate(walletData: WalletData)
    fun onUsdUpdate(scPriceData: ScPriceData)
    fun onTransactionsUpdate(transactionsData: TransactionsData)
    fun onConsensusUpdate(consensusData: ConsensusData)
    fun onWalletError(error: SiaError)
    fun onUsdError(error: SiaError)
    fun onTransactionsError(error: SiaError)
    fun onConsensusError(error: SiaError)
}