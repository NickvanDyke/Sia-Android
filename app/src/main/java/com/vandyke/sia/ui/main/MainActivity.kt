/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
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
import com.vandyke.sia.ui.common.ComingSoonFragment
import com.vandyke.sia.ui.node.NodeStatusFragment
import com.vandyke.sia.ui.node.modules.NodeModulesFragment
import com.vandyke.sia.ui.node.settings.NodeSettingsFragmentContainer
import com.vandyke.sia.ui.onboarding.IntroActivity
import com.vandyke.sia.ui.onboarding.PurchaseActivity
import com.vandyke.sia.ui.renter.allowance.AllowanceFragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Prefs.viewedOnboarding) {
            finish()
            startActivity(Intent(this, IntroActivity::class.java))
            return
        }

        if (!BuildConfig.DEBUG)
            checkPurchases()

        /* allow rotation in debug builds, for easy recreation testing */
        if (BuildConfig.DEBUG)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        /* migrate from the old use-external-storage method if necessary.
         * We can delete Prefs.useExternal and StorageUtil.getExternalStorage when this is no longer needed */
        if (Prefs.useExternal) {
            Prefs.siaWorkingDirectory = StorageUtil.getExternalStorage(this).absolutePath
            Prefs.useExternal = false
        }

        AppCompatDelegate.setDefaultNightMode(if (Prefs.darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        setTheme(if (Prefs.oldSiaColors) R.style.AppTheme_DayNight_OldSiaColors else R.style.AppTheme_DayNight)
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
                    displayFragment(if (BuildConfig.DEBUG) FilesFragment::class.java else ComingSoonFragment::class.java)
                    3L // doesn't fire the listener? Maybe since it's in a submenu. So we set it manually above
                }
                "wallet" -> 2L
                else -> throw IllegalArgumentException("Invalid startup page: ${Prefs.startupPage}")
            }, true)
        } else {
            /* find the fragment currently visible stored in the savedInstanceState */
            val storedFragmentClass = supportFragmentManager.findFragmentByTag(savedInstanceState.getString("visibleFragment")).javaClass
            displayFragment(storedFragmentClass)
            drawer.setSelection(savedInstanceState.getLong("drawerSelectedId"), false)
        }

        if (Prefs.displayedTransaction && !Prefs.shownFeedbackDialog) {
            DialogUtil.showRateDialog(this)
            Prefs.shownFeedbackDialog = true
        }
    }

    fun displayFragment(clazz: Class<*>) {
        /* return if the currently visible fragment is the same class as the one we want to display */
        if (clazz == visibleFragment?.javaClass)
            return
        val tx = supportFragmentManager.beginTransaction()
        if (visibleFragment != null)
            tx.hide(visibleFragment)
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
                        val purchased = purchases.purchasesList?.any { it.sku == PurchaseActivity.overall_sub_sku } == true
                        if (purchased)
                            Prefs.requirePurchaseAt = 0
                        if (!purchased && System.currentTimeMillis() > Prefs.requirePurchaseAt) {
                            finish()
                            startActivity(Intent(this@MainActivity, PurchaseActivity::class.java))
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
                            .withIdentifier(3)
                            .withOnDrawerItemClickListener { _, _, _ ->
                                displayFragment(if (BuildConfig.DEBUG)
                                    FilesFragment::class.java
                                else
                                    ComingSoonFragment::class.java)
                                false
                            },
                    SecondaryDrawerItem()
                            .withName("Allowance")
                            .withIcon(R.drawable.ic_money_white)
                            .withIconTintingEnabled(true)
                            .withSelectedIconColor(colorPrimary)
                            .withSelectedTextColor(colorPrimary)
                            .withIdentifier(1)
                            .withOnDrawerItemClickListener { _, _, _ ->
                                displayFragment(if (BuildConfig.DEBUG)
                                    AllowanceFragment::class.java
                                else
                                    ComingSoonFragment::class.java)
                                false
                            })

            primaryItem {
                name = "Wallet"
                icon = R.drawable.ic_account_balance_wallet_white
                iconTintingEnabled = true
                selectedIconColor = colorPrimary.toLong()
                selectedTextColor = colorPrimary.toLong()
                identifier = 2
                onClick { view -> displayFragment(WalletFragment::class.java); false }
            }

            divider { }

            primaryItem {
                name = "Settings"
                icon = R.drawable.ic_settings_black
                selectedIconColor = colorPrimary.toLong()
                selectedTextColor = colorPrimary.toLong()
                iconTintingEnabled = true
                onClick { view -> displayFragment(SettingsFragmentContainer::class.java); false }
            }

            primaryItem {
                name = "About"
                icon = R.drawable.ic_info_outline_black
                selectedIconColor = colorPrimary.toLong()
                selectedTextColor = colorPrimary.toLong()
                iconTintingEnabled = true
                onClick { view -> displayFragment(AboutFragment::class.java); false }
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
            outState?.putString("visibleFragment", visibleFragment!!.javaClass.simpleName)
        outState?.putLong("drawerSelectedId", drawer.currentSelection)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else if (visibleFragment?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }
}
