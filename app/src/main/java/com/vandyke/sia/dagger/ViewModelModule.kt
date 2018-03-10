/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.dagger

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.vandyke.sia.ui.node.modules.NodeModulesViewModel
import com.vandyke.sia.ui.renter.allowance.AllowanceViewModel
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.ui.wallet.viewmodel.WalletViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(WalletViewModel::class)
    abstract fun bindWalletViewModel(walletViewModel: WalletViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FilesViewModel::class)
    abstract fun bindFilesViewModel(filesViewModel: FilesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AllowanceViewModel::class)
    abstract fun bindAllowanceViewModel(allowanceViewModel: AllowanceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NodeModulesViewModel::class)
    abstract fun bindNodeModulesViewModel(nodeModulesViewModel: NodeModulesViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: SiaViewModelFactory): ViewModelProvider.Factory
}