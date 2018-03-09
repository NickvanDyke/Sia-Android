/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.dagger

import com.vandyke.sia.App
import com.vandyke.sia.data.siad.SiadService
import com.vandyke.sia.ui.node.NodeStatusFragment
import com.vandyke.sia.ui.renter.allowance.AllowanceFragment
import com.vandyke.sia.ui.renter.files.view.FilesFragment
import com.vandyke.sia.ui.settings.SettingsFragment
import com.vandyke.sia.ui.terminal.TerminalFragment
import com.vandyke.sia.ui.wallet.view.WalletFragment
import dagger.Component
import javax.inject.Singleton

// TODO: use AndroidInjection stuff. https://google.github.io/dagger/android and
// https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample/app/src/main/java/com/android/example/github/di
@Singleton
@Component(modules = [
    SiaModule::class,
    DbModule::class,
    AppModule::class,
    ViewModelModule::class
])
interface AppComponent {
    fun inject(walletFragment: WalletFragment)
    fun inject(filesFragment: FilesFragment)
    fun inject(terminalFragment: TerminalFragment)
    fun inject(allowanceFragment: AllowanceFragment)
    fun inject(nodeStatusFragment: NodeStatusFragment)
    fun inject(settingsFragment: SettingsFragment)
    fun inject(siadService: SiadService)
    fun inject(app: App)
}