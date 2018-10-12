/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.vandyke.sia.BuildConfig
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadService
import com.vandyke.sia.ui.about.AboutFragment
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.help.HelpFragment
import com.vandyke.sia.ui.node.NodeStatusFragment
import com.vandyke.sia.ui.node.modules.NodeModulesFragment
import com.vandyke.sia.ui.node.settings.NodeSettingsFragmentContainer
import com.vandyke.sia.ui.purchase.PurchaseDialog
import com.vandyke.sia.ui.renter.allowance.AllowanceFragment
import com.vandyke.sia.ui.renter.contracts.view.ContractsFragment
import com.vandyke.sia.ui.renter.files.view.FilesFragment
import com.vandyke.sia.ui.settings.SettingsFragmentContainer
import com.vandyke.sia.ui.wallet.view.WalletFragment
import com.vandyke.sia.util.DialogUtil
import com.vandyke.sia.util.SiaUtil
import com.vandyke.sia.util.StorageUtil
import com.vandyke.sia.util.getAttrColor
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var visibleFragment: BaseFragment? = null
    private lateinit var drawer: Drawer
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "modulesString" && Prefs.modulesString.contains('r') && !Prefs.viewedFirstTimeLoadingRenter)
            showFirstTimeRenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!BuildConfig.DEBUG && supportFragmentManager.findFragmentByTag(PURCHASE_DIALOG_TAG) == null) {
            checkPurchases()
        }

        /* allow rotation in debug builds, for easy recreation testing */
        if (BuildConfig.DEBUG)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        /* migrate from the old use-external-storage method if necessary.
         * We can delete Prefs.useExternal and StorageUtil.getExternalStorage when this is no longer needed */
        if (Prefs.useExternal) {
            Prefs.siaWorkingDirectory = StorageUtil.getExternalStorage(this).absolutePath
            Prefs.useExternal = false
        }

        AppCompatDelegate.setDefaultNightMode(when {
            Prefs.darkMode -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_NO
        })
        setTheme(when {
            Prefs.oldSiaColors -> R.style.AppTheme_DayNight_OldSiaColors
            else -> R.style.AppTheme_DayNight
        })
        setContentView(R.layout.activity_main)

        if (!BuildConfig.DEBUG && !SiaUtil.isSiadSupported) {
            AlertDialog.Builder(this)
                    .setTitle("Sia node unsupported")
                    .setMessage("Your device isn't able to run the Sia node. Only devices that can are able to download" +
                            " this app from the Play Store, so you must have obtained it some other way. Sorry.")
                    .setCancelable(false)
                    .setPositiveButton("Close", null)
                    .show()
        } else {
            startService(Intent(this, SiadService::class.java))
        }

        setupDrawer()

        /* display the appropriate initial fragment */
        if (savedInstanceState == null) {
            drawer.setSelection(when (Prefs.startupPage) {
                "files" -> {
                    displayFragment(FilesFragment::class.java)
                    DRAWER_ID_FILES // doesn't fire the listener? Maybe since it's in a submenu. So we set it manually above
                }
                "wallet" -> DRAWER_ID_WALLET
                else -> throw IllegalArgumentException("Invalid startup page: ${Prefs.startupPage}")
            }, true)
        } else {
            val storedFragmentClass = supportFragmentManager.findFragmentByTag(savedInstanceState.getString(VISIBLE_FRAGMENT_KEY))!!.javaClass
            displayFragment(storedFragmentClass)
            drawer.setSelection(savedInstanceState.getLong(DRAWER_SELECTED_ID_KEY), false)
        }

        if (Prefs.displayedTransaction && !Prefs.shownFeedbackDialog) {
            DialogUtil.showRateDialog(this)
            Prefs.shownFeedbackDialog = true
        }

        Prefs.preferences.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    fun displayFragment(clazz: Class<*>) {
        /* return if the currently visible fragment is the same class as the one we want to display */
        if (clazz == visibleFragment?.javaClass)
            return
        val tx = supportFragmentManager.beginTransaction()
        visibleFragment?.let { tx.hide(it) }
        /* check if the to-be-displayed fragment already exists in the fragment manager */
        var newFragment = supportFragmentManager.findFragmentByTag(clazz.simpleName) as? BaseFragment
        /* if not, create an instance of it and add it to the frame */
        if (newFragment == null) {
            newFragment = clazz.newInstance() as BaseFragment
            tx.add(R.id.fragment_frame, newFragment, clazz.simpleName)
        } else {
            tx.show(newFragment)
        }
        tx.commit()
        visibleFragment = newFragment

        supportActionBar!!.title = visibleFragment!!.title
    }

    private fun checkPurchases() {
        val client = BillingClient.newBuilder(this).setListener({ _, _ ->
            /* we don't make purchases here so we don't care about listening for updates. Required to set a listener though. */
        }).build()
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(responseCode: Int) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    val purchases = client.queryPurchases(BillingClient.SkuType.SUBS)
                    if (purchases.responseCode == BillingClient.BillingResponse.OK) {
                        val purchased = purchases.purchasesList?.any { it.sku == PurchaseDialog.overall_sub_sku } == true
                        if (purchased) {
                            Prefs.requirePurchaseAt = 0
                        } else if (System.currentTimeMillis() > Prefs.requirePurchaseAt) {
                            displayPurchasePrompt()
                            // TODO: maybe stop SiadService here? Because due to the time that checking purchases takes, it will have started by now
                        }
                    }
                }
                if (client.isReady)
                    client.endConnection()
            }

            override fun onBillingServiceDisconnected() {
            }
        })
    }

    private fun displayPurchasePrompt() {
        supportFragmentManager.beginTransaction()
                .add(PurchaseDialog(), PURCHASE_DIALOG_TAG)
                .commitAllowingStateLoss()
    }

    private fun setupDrawer() {
        setSupportActionBar(toolbar)

        val colorPrimary = getAttrColor(R.attr.colorPrimary)

        drawer = drawer {
            headerViewRes = R.layout.drawer_header
            translucentStatusBar = false
            toolbar = this@MainActivity.toolbar
            headerDivider = false
            widthDp = 225

            primaryItem {
                name = "Node"
                icon = R.drawable.ic_dns_white
                iconTintingEnabled = true
                selectable = false
            }.withSubItems(
                    SecondaryDrawerItem()
                            .withName("Status")
                            .withIcon(R.drawable.ic_short_text_black)
                            .withIconTintingEnabled(true)
                            .withSelectedIconColor(colorPrimary)
                            .withSelectedTextColor(colorPrimary)
                            .withOnDrawerItemClickListener { _, _, _ -> displayFragment(NodeStatusFragment::class.java); false },
                    SecondaryDrawerItem()
                            .withName("Modules")
                            .withIcon(R.drawable.ic_storage_white)
                            .withIconTintingEnabled(true)
                            .withSelectedIconColor(colorPrimary)
                            .withSelectedTextColor(colorPrimary)
                            .withOnDrawerItemClickListener { _, _, _ -> displayFragment(NodeModulesFragment::class.java); false },
                    SecondaryDrawerItem()
                            .withName("Settings")
                            .withIcon(R.drawable.ic_settings_black)
                            .withIconTintingEnabled(true)
                            .withSelectedIconColor(colorPrimary)
                            .withSelectedTextColor(colorPrimary)
                            .withOnDrawerItemClickListener { _, _, _ -> displayFragment(NodeSettingsFragmentContainer::class.java); false })

            divider { }

            primaryItem {
                name = "Renter"
                icon = R.drawable.ic_cloud_black
                iconTintingEnabled = true
                selectable = false
            }.withSubItems(
                    SecondaryDrawerItem()
                            .withName("Files")
                            .withIcon(R.drawable.ic_folder_white)
                            .withIconTintingEnabled(true)
                            .withSelectedIconColor(colorPrimary)
                            .withSelectedTextColor(colorPrimary)
                            .withIdentifier(DRAWER_ID_FILES)
                            .withOnDrawerItemClickListener { _, _, _ ->
                                displayFragment(FilesFragment::class.java)
                                false
                            },
                    SecondaryDrawerItem()
                            .withName("Allowance")
                            .withIcon(R.drawable.ic_money_white)
                            .withIconTintingEnabled(true)
                            .withSelectedIconColor(colorPrimary)
                            .withSelectedTextColor(colorPrimary)
                            .withIdentifier(DRAWER_ID_ALLOWANCE)
                            .withOnDrawerItemClickListener { _, _, _ ->
                                displayFragment(AllowanceFragment::class.java)
                                false
                            },
                    SecondaryDrawerItem()
                            .withName("Contracts")
                            .withIcon(R.drawable.ic_file_white)
                            .withIconTintingEnabled(true)
                            .withSelectedIconColor(colorPrimary)
                            .withSelectedTextColor(colorPrimary)
                            .withOnDrawerItemClickListener { _, _, _ ->
                                displayFragment(ContractsFragment::class.java)
                                false
                            })

            primaryItem {
                name = "Wallet"
                icon = R.drawable.ic_account_balance_wallet_white
                iconTintingEnabled = true
                selectedIconColor = colorPrimary.toLong()
                selectedTextColor = colorPrimary.toLong()
                identifier = DRAWER_ID_WALLET
                onClick { _ -> displayFragment(WalletFragment::class.java); false }
            }

            divider { }

            primaryItem {
                name = "Settings"
                icon = R.drawable.ic_settings_black
                selectedIconColor = colorPrimary.toLong()
                selectedTextColor = colorPrimary.toLong()
                iconTintingEnabled = true
                onClick { _ -> displayFragment(SettingsFragmentContainer::class.java); false }
            }

            primaryItem {
                name = "About"
                icon = R.drawable.ic_info_outline_black
                selectedIconColor = colorPrimary.toLong()
                selectedTextColor = colorPrimary.toLong()
                iconTintingEnabled = true
                onClick { _ -> displayFragment(AboutFragment::class.java); false }
            }

            primaryItem {
                name = "Help"
                icon = R.drawable.ic_help_outline_black
                selectedIconColor = colorPrimary.toLong()
                selectedTextColor = colorPrimary.toLong()
                iconTintingEnabled = true
                onClick { _ -> displayFragment(HelpFragment::class.java); false }
            }
        }
    }

    fun deselectDrawer() {
        drawer.deselect()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        /* save the visible fragment, to be retrieved in onCreate */
        if (visibleFragment != null)
            outState?.putString(VISIBLE_FRAGMENT_KEY, visibleFragment!!.javaClass.simpleName)
        outState?.putLong(DRAWER_SELECTED_ID_KEY, drawer.currentSelection)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else if (visibleFragment?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }

    private fun showFirstTimeRenter() {
        AlertDialog.Builder(this)
                .setTitle("Notice")
                .setMessage("This is your first time loading the renter module, which will" +
                        " take a significant amount of time. This is normal, and" +
                        " subsequent starts of the renter module will be much, much quicker. You are free" +
                        " to keep Sia in the background while it loads. Thanks for your patience!")
                .setPositiveButton(android.R.string.ok) { _, _ -> Prefs.viewedFirstTimeLoadingRenter = true }
                .setCancelable(false)
                .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Prefs.preferences.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    companion object {
        const val DRAWER_ID_FILES = 1L
        const val DRAWER_ID_ALLOWANCE = 2L
        const val DRAWER_ID_WALLET = 3L

        private const val VISIBLE_FRAGMENT_KEY = "VISIBLE_FRAGMENT"
        private const val DRAWER_SELECTED_ID_KEY = "DRAWER_SELECTED_ID_KEY"

        private const val PURCHASE_DIALOG_TAG = "PURCHASE_DIALOG"
    }
}
