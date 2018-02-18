/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.vandyke.sia.BuildConfig
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.ui.about.AboutFragment
import com.vandyke.sia.ui.common.BaseFragment
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
//            startService(Intent(this, SiadService::class.java))
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

        supportActionBar!!.title = visibleFragment!!.javaClass.simpleName.removeSuffix("Fragment")
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

        val filesItem = SecondaryDrawerItem()
                .withIsExpanded(true)
                .withName("Files")
                .withIcon(R.drawable.ic_folder)
                .withIconTintingEnabled(true)
                .withIdentifier(0)
                .withOnDrawerItemClickListener { _, _, _ -> displayFragment(FilesFragment::class.java); false }

        val allowanceItem = SecondaryDrawerItem()
                .withName("Allowance")
                .withIcon(R.drawable.ic_money)
                .withIconTintingEnabled(true)
                .withIdentifier(1)
                .withOnDrawerItemClickListener { _, _, _ -> displayFragment(AllowanceFragment::class.java); false }

        // TODO: make Renter item start expanded. Seems to be buggy
        val renterItem = PrimaryDrawerItem()
                .withName("Renter")
                .withIcon(R.drawable.ic_cloud)
                .withIconTintingEnabled(true)
                .withIdentifier(2)
                .withSubItems(filesItem, allowanceItem)
                .withSelectable(false)
                .withOnDrawerItemClickListener { _, _, _ -> true }

        val walletItem = PrimaryDrawerItem()
                .withName("Wallet")
                .withIcon(R.drawable.ic_account_balance_wallet)
                .withIconTintingEnabled(true)
                .withIdentifier(3)
                .withOnDrawerItemClickListener { _, _, _ -> displayFragment(WalletFragment::class.java); false }

        val terminalItem = PrimaryDrawerItem()
                .withName("Terminal")
                .withIcon(R.drawable.icon_terminal)
                .withIconTintingEnabled(true)
                .withIdentifier(4)
                .withOnDrawerItemClickListener { _, _, _ -> displayFragment(TerminalFragment::class.java); false }

        val settingsItem = PrimaryDrawerItem()
                .withName("Settings")
                .withIcon(R.drawable.ic_settings)
                .withIconTintingEnabled(true)
                .withIdentifier(5)
                .withOnDrawerItemClickListener { _, _, _ -> displayFragment(SettingsFragment::class.java); false }

        val aboutItem = PrimaryDrawerItem()
                .withName("About")
                .withIcon(R.drawable.ic_info_outline)
                .withIconTintingEnabled(true)
                .withIdentifier(6)
                .withOnDrawerItemClickListener { _, _, _ -> displayFragment(AboutFragment::class.java); false }

        drawer = DrawerBuilder()
                .withActivity(this)
                .withHeader(View.inflate(this, R.layout.drawer_header, null))
                .withTranslucentStatusBar(false)
                .addDrawerItems(renterItem, walletItem, terminalItem, DividerDrawerItem(), settingsItem, aboutItem)
                .withToolbar(toolbar)
                .withCloseOnClick(true)
                .withHeaderDivider(false)
                .withDrawerWidthDp(225)
                .build()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        /* save the visible fragment, to be retrieved in onCreate */
        if (visibleFragment != null) {
            outState?.putString("visibleFragment", visibleFragment!!.javaClass.simpleName)
        }
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
