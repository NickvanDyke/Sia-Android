/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.mikepenz.materialdrawer.Drawer
import com.vandyke.sia.BuildConfig
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadService
import com.vandyke.sia.ui.about.AboutFragment
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.common.ComingSoonFragment
import com.vandyke.sia.ui.node.NodeFragment
import com.vandyke.sia.ui.onboarding.IntroActivity
import com.vandyke.sia.ui.onboarding.PurchaseActivity
import com.vandyke.sia.ui.renter.allowance.AllowanceFragment
import com.vandyke.sia.ui.renter.files.view.FilesFragment
import com.vandyke.sia.ui.settings.SettingsFragment
import com.vandyke.sia.ui.terminal.TerminalFragment
import com.vandyke.sia.ui.wallet.view.WalletFragment
import com.vandyke.sia.util.DialogUtil
import com.vandyke.sia.util.SiaUtil
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

        AppCompatDelegate.setDefaultNightMode(
                if (Prefs.darkMode)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
        )
        setTheme(R.style.AppTheme_DayNight)
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

        if (savedInstanceState == null) {
            drawer.setSelection(when (Prefs.startupPage) {
                "files" -> {
                    displayFragment(FilesFragment::class.java)
                    0L // doesn't fire the listener? Maybe since it's in a submenu. So we set it manually above
                }
                "wallet" -> 3L
                "terminal" -> 4L
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

    private fun displayFragment(clazz: Class<*>) {
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
                        val purchased = purchases.purchasesList?.find { it.sku == PurchaseActivity.overall_sub_sku } != null
                        if (purchased)
                            Prefs.requirePurchaseAt = 0
                        if (!purchased && System.currentTimeMillis() > Prefs.requirePurchaseAt) {
                            finish()
                            startActivity(Intent(this@MainActivity, PurchaseActivity::class.java))
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

        drawer = drawer {
            headerViewRes = R.layout.drawer_header
            translucentStatusBar = false
            toolbar = this@MainActivity.toolbar
            headerDivider = false
            widthDp = 225

            primaryItem {
                name = "Node"
                icon = R.drawable.sia_new_circle_logo_transparent_white
                iconTintingEnabled = true
                identifier = -1
                onClick { view -> displayFragment(NodeFragment::class.java); false }
            }

            primaryItem {
                name = "Renter"
                icon = R.drawable.ic_cloud
                iconTintingEnabled = true
                identifier = 2
                selectable = false
                onClick { view -> true }
            }.withSubItems(
                    secondaryItem {
                        name = "Files"
                        icon = R.drawable.ic_folder
                        iconTintingEnabled = true
                        identifier = 0
                        onClick { view ->
                            displayFragment(if (BuildConfig.DEBUG)
                                FilesFragment::class.java
                            else
                                ComingSoonFragment::class.java)
                            false
                        }
                    },
                    secondaryItem {
                        name = "Allowance"
                        icon = R.drawable.ic_money
                        iconTintingEnabled = true
                        identifier = 1
                        onClick { view ->
                            displayFragment(if (BuildConfig.DEBUG)
                                AllowanceFragment::class.java
                            else
                                ComingSoonFragment::class.java)
                            false
                        }
                    })

            primaryItem {
                name = "Wallet"
                icon = R.drawable.ic_account_balance_wallet
                iconTintingEnabled = true
                identifier = 3
                onClick { view -> displayFragment(WalletFragment::class.java); false }
            }

            primaryItem {
                name = "Terminal"
                icon = R.drawable.icon_terminal
                iconTintingEnabled = true
                identifier = 4
                onClick { view -> displayFragment(TerminalFragment::class.java); false }
            }

            primaryItem {
                name = "Settings"
                icon = R.drawable.ic_settings
                iconTintingEnabled = true
                onClick { view -> displayFragment(SettingsFragment::class.java); false }
            }

            primaryItem {
                name = "About"
                icon = R.drawable.ic_info_outline
                iconTintingEnabled = true
                onClick { view -> displayFragment(AboutFragment::class.java); false }
            }
        }
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
