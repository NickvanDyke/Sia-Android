/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.siamobile.ui.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.view.MenuItem
import com.vandyke.siamobile.R
import com.vandyke.siamobile.isSiadLoaded
import com.vandyke.siamobile.ui.about.AboutFragment
import com.vandyke.siamobile.ui.hosting.fragments.HostingFragment
import com.vandyke.siamobile.ui.renter.main.RenterFragment
import com.vandyke.siamobile.ui.settings.SettingsFragment
import com.vandyke.siamobile.ui.terminal.TerminalFragment
import com.vandyke.siamobile.ui.wallet.view.WalletFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class MainViewModel : ViewModel() {
    val siadIsLoading = MutableLiveData<Boolean>()
    val visibleFragmentClass = MutableLiveData<Class<*>>()
    val title = MutableLiveData<String>()
    val selectedMenuItem = MutableLiveData<Int>()

    private val subscription: Disposable
    init {
        subscription = isSiadLoaded.observeOn(AndroidSchedulers.mainThread()).subscribe {
            siadIsLoading.value = !it
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }
    
    fun navigationItemSelected(item: MenuItem) {
        visibleFragmentClass.value = when (item.itemId) {
            R.id.drawer_item_renter -> RenterFragment::class.java
            R.id.drawer_item_hosting -> HostingFragment::class.java
            R.id.drawer_item_wallet -> WalletFragment::class.java
            R.id.drawer_item_terminal -> TerminalFragment::class.java
            R.id.drawer_item_settings -> SettingsFragment::class.java
            R.id.drawer_item_about -> AboutFragment::class.java
            else -> throw Exception()
        }
        setTitleAndMenuFromVisibleFragment()
    }

    fun setDisplayedFragmentClass(clazz: Class<*>) {
        visibleFragmentClass.value = clazz
        setTitleAndMenuFromVisibleFragment()
    }

    private fun setTitleAndMenuFromVisibleFragment() {
        val clazz = visibleFragmentClass.value ?: return
        title.value = clazz.simpleName.replace("Fragment", "")
        selectedMenuItem.value = when (clazz) {
            RenterFragment::class.java -> R.id.drawer_item_renter
            HostingFragment::class.java -> R.id.drawer_item_hosting
            WalletFragment::class.java -> R.id.drawer_item_wallet
            TerminalFragment::class.java -> R.id.drawer_item_terminal
            SettingsFragment::class.java -> R.id.drawer_item_settings
            AboutFragment::class.java -> R.id.drawer_item_about
            else -> throw Exception() /* not sure what this should actually be, if anything */
        }
    }
}