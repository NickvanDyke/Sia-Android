/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.dagger

import com.vandyke.sia.ui.renter.files.view.FilesFragment
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
}