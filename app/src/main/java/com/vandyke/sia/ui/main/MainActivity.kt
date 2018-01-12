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
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.MenuItem
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadService
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.renter.files.view.FilesFragment
import com.vandyke.sia.ui.terminal.TerminalFragment
import com.vandyke.sia.ui.wallet.view.WalletFragment
import com.vandyke.sia.util.observe
import de.cketti.library.changelog.ChangeLog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var visibleFragment: BaseFragment? = null
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        startService(Intent(this, SiadService::class.java))

        /* appearance stuff */
        if (Prefs.darkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setTheme(R.style.AppTheme_DayNight)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
        navigationView.setNavigationItemSelectedListener({ item ->
            drawerLayout.closeDrawers()
            viewModel.navigationItemSelected(item)
            return@setNavigationItemSelectedListener true
        })
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)

        /* set the VM's visibleFragmentClass differently depending on whether the activity is being recreated */
        if (savedInstanceState == null) {
            viewModel.setDisplayedFragmentClass(when (Prefs.startupPage) {
                "renter" -> FilesFragment::class.java
                "wallet" -> WalletFragment::class.java
                "terminal" -> TerminalFragment::class.java
                else -> throw Exception()
            })
        } else {
            /* find the fragment currently visible stored in the savedInstanceState */
            val storedFragmentClass = supportFragmentManager.findFragmentByTag(savedInstanceState.getString("visibleFragment")).javaClass
            viewModel.setDisplayedFragmentClass(storedFragmentClass)
        }

        /* changelog stuff */
        val changelog = ChangeLog(this)
        /* if this is the user's first time ever running the app, there isn't much point to showing
           them a changelog, so skip it until they update */
        if (changelog.isFirstRunEver) {
            changelog.skipLogDialog()
        } else if (changelog.isFirstRun) {
            changelog.logDialog.show()
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
        /* check if the to-be-displayed fragment already exists */
        var newFragment = supportFragmentManager.findFragmentByTag(clazz.simpleName) as? BaseFragment
        /* if not, create an instance of it and add it to the frame */
        if (newFragment == null) {
            newFragment = clazz.newInstance() as BaseFragment
            tx.add(R.id.fragment_frame, newFragment, clazz.simpleName)
        } else {
            tx.show(newFragment)
        }
        if (visibleFragment != null)
            tx.hide(visibleFragment)
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
