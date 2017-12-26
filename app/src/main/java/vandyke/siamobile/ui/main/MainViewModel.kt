/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.view.MenuItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import vandyke.siamobile.R
import vandyke.siamobile.data.siad.SiadService
import vandyke.siamobile.ui.about.AboutFragment
import vandyke.siamobile.ui.hosting.fragments.HostingFragment
import vandyke.siamobile.ui.renter.main.RenterFragment
import vandyke.siamobile.ui.settings.SettingsFragment
import vandyke.siamobile.ui.terminal.TerminalFragment
import vandyke.siamobile.ui.wallet.view.WalletFragment

class MainViewModel : ViewModel() {
    val siadIsLoading = MutableLiveData<Boolean>()
    val visibleFragmentClass = MutableLiveData<Class<*>>()
    val title = MutableLiveData<String>()
    val selectedMenuItem = MutableLiveData<Int>()

    private val subscription: Disposable
    init {
        subscription = SiadService.isSiadLoaded.observeOn(AndroidSchedulers.mainThread()).subscribe {
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