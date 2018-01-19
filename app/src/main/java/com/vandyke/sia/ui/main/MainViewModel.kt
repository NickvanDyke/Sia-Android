/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.view.MenuItem
import com.vandyke.sia.R
import com.vandyke.sia.ui.about.AboutFragment
import com.vandyke.sia.ui.renter.files.view.FilesFragment
import com.vandyke.sia.ui.settings.SettingsFragment
import com.vandyke.sia.ui.terminal.TerminalFragment
import com.vandyke.sia.ui.wallet.view.WalletFragment

class MainViewModel : ViewModel() {
    val visibleFragmentClass = MutableLiveData<Class<*>>()
    val title = MutableLiveData<String>()
    val selectedMenuItem = MutableLiveData<Int>()
    
    fun navigationItemSelected(item: MenuItem) {
        visibleFragmentClass.value = when (item.itemId) {
            R.id.drawer_item_renter -> FilesFragment::class.java
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
            FilesFragment::class.java -> R.id.drawer_item_renter
            WalletFragment::class.java -> R.id.drawer_item_wallet
            TerminalFragment::class.java -> R.id.drawer_item_terminal
            SettingsFragment::class.java -> R.id.drawer_item_settings
            AboutFragment::class.java -> R.id.drawer_item_about
            else -> throw Exception() /* not sure what this should actually be, if anything */
        }
    }
}