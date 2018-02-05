/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.MenuItem
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.vandyke.sia.BuildConfig
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadService
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.onboarding.IntroActivity
import com.vandyke.sia.ui.onboarding.PurchaseActivity
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.observe
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var visibleFragment: BaseFragment? = null
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Prefs.viewedOnboarding) {
            finish()
            startActivity(Intent(this, IntroActivity::class.java))
            return
        }

        if (!BuildConfig.DEBUG) {
            if (!Prefs.cachedPurchased && (Prefs.requirePurchaseAt == 0L || System.currentTimeMillis() > Prefs.requirePurchaseAt)) {
                finish()
                startActivity(Intent(this, PurchaseActivity::class.java))
                return
            }
            checkPurchases()
        }

        AppCompatDelegate.setDefaultNightMode(
                if (Prefs.darkMode)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
        )
        setTheme(R.style.AppTheme_DayNight)
        setContentView(R.layout.activity_main)

        if (!GenUtil.isSiadSupported) {
            AlertDialog.Builder(this)
                    .setTitle("Sia node unsupported")
                    .setMessage("Your device isn't able to run the Sia node. Only devices that can are able to download" +
                            " this app from the Play Store, so you must have obtained it some other way. Sorry.")
                    .setPositiveButton("Close", null)
                    .show()
        } else {
            startService(Intent(this, SiadService::class.java))
        }

        /* actionbar setup stuff */
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel.visibleFragmentClass.observe(this) {
            displayFragment(it)
        }

        viewModel.title.observe(this) {
            supportActionBar!!.title = it
        }

        viewModel.selectedMenuItem.observe(this) {
            navigationView.setCheckedItem(it)
        }

        /* notify VM when navigation items are selected */
        navigationView.setNavigationItemSelectedListener(
                { item ->
                    drawerLayout.closeDrawers()
                    viewModel.navigationItemSelected(item.itemId)
                    return@setNavigationItemSelectedListener true
                })
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)

        /* set the VM's visibleFragmentClass differently depending on whether the activity is being recreated */
        if (savedInstanceState == null) {
            viewModel.navigationItemSelected(
                    when (Prefs.startupPage) {
                        "renter" -> R.id.drawer_item_renter
                        "wallet" -> R.id.drawer_item_wallet
                        "terminal" -> R.id.drawer_item_terminal
                        else -> throw Exception()
                    })
        } else {
            /* find the fragment currently visible stored in the savedInstanceState */
            val storedFragmentClass = supportFragmentManager.findFragmentByTag(savedInstanceState.getString("visibleFragment")).javaClass
            viewModel.setDisplayedFragmentClass(storedFragmentClass)
        }

        Prefs.timesStarted++
        if (Prefs.timesStarted > 7 && !Prefs.shownFeedbackDialog) {
            GenUtil.showRateDialog(this)
            Prefs.shownFeedbackDialog = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        /* save the visible fragment, to be retrieved in onCreate */
        if (visibleFragment != null)
            outState?.putString("visibleFragment", visibleFragment!!.javaClass.simpleName)
    }

    private fun displayFragment(clazz: Class<*>) {
        /* return if the currently visible fragment is the same class as the one we want to display */
        if (clazz == visibleFragment?.javaClass)
            return
        val tx = supportFragmentManager.beginTransaction()
        if (visibleFragment != null)
            tx.hide(visibleFragment)
        /* check if the to-be-displayed fragment already exists */
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
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (visibleFragment?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }

    private fun checkPurchases() {
        val client = BillingClient.newBuilder(this).setListener({ responseCode, purchases ->
            /* we don't make purchases here so we don't care about listening for updates. Required to set a listener though. */
        }).build()
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(responseCode: Int) {
                println(responseCode)
                if (responseCode == BillingClient.BillingResponse.OK) {
                    val purchases = client.queryPurchases(BillingClient.SkuType.SUBS)
                    if (purchases.responseCode == BillingClient.BillingResponse.OK) {
                        val purchased = purchases.purchasesList?.find { it.sku == PurchaseActivity.overall_sub_sku } != null
                        Prefs.cachedPurchased = purchased
                        if (!purchased && (Prefs.requirePurchaseAt == 0L || Prefs.requirePurchaseAt < System.currentTimeMillis())) {
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

    /* below methods are for drawer stuff */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }
}
