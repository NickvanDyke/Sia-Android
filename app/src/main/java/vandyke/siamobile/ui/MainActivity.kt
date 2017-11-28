/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.siad.SiadService
import vandyke.siamobile.ui.about.AboutFragment
import vandyke.siamobile.ui.about.SetupRemoteActivity
import vandyke.siamobile.ui.hosting.fragments.HostingFragment
import vandyke.siamobile.ui.renter.view.RenterFragment
import vandyke.siamobile.ui.settings.ModesActivity
import vandyke.siamobile.ui.settings.Prefs
import vandyke.siamobile.ui.settings.SettingsFragment
import vandyke.siamobile.ui.terminal.TerminalFragment
import vandyke.siamobile.ui.wallet.view.PaperWalletActivity
import vandyke.siamobile.ui.wallet.view.WalletFragment
import vandyke.siamobile.util.StorageUtil

class MainActivity : AppCompatActivity() {

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private var visibleFragment: BaseFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        /* appearance stuff */
        if (Prefs.darkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setTheme(R.style.AppTheme_DayNight)

        /* pass super null so that it doesn't attempt to recreate fragments. I checked the source
         * code of the super methods, and it seems it mostly just recreates fragments from the savedInstanceState,
         * which is exactly what I don't want it to do, so this shouldn't have any weird side-effects */
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        if (Prefs.transparentBars) {
            toolbar.setBackgroundColor(resources.getColor(android.R.color.transparent))
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }

        defaultTextColor = TextView(this).currentTextColor
        /* set action stuff for when drawer items are selected */
        navigationView.setNavigationItemSelectedListener({ item ->
            drawerLayout.closeDrawers()
            when (item.itemId) {
                R.id.drawer_item_renter -> displayFragment(RenterFragment::class.java)
                R.id.drawer_item_hosting -> displayFragment(HostingFragment::class.java)
                R.id.drawer_item_wallet -> displayFragment(WalletFragment::class.java)
                R.id.drawer_item_terminal -> displayFragment(TerminalFragment::class.java)
                R.id.drawer_item_settings -> displayFragment(SettingsFragment::class.java)
                R.id.drawer_item_about -> displayFragment(AboutFragment::class.java)
            }
            return@setNavigationItemSelectedListener true
        })
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)

        if (Prefs.operationMode == "local_full_node")
            startService(Intent(this, SiadService::class.java))

        if (savedInstanceState == null) {
            when (Prefs.startupPage) {
                "renter" -> displayFragment(RenterFragment::class.java)
                "hosting" -> displayFragment(HostingFragment::class.java)
                "wallet" -> displayFragment(WalletFragment::class.java)
                "terminal" -> displayFragment(TerminalFragment::class.java)
            }
        } else {
            /* find the fragment currently visible stored in the savedInstanceState */
            visibleFragment = supportFragmentManager.findFragmentByTag(savedInstanceState.getString("visibleFragment")) as BaseFragment
            setTitleAndMenuFromVisibleFragment()
        }

        if (Prefs.firstTime) {
            startActivityForResult(Intent(this, ModesActivity::class.java), REQUEST_OPERATION_MODE)
//            startActivity(Intent(this, AboutSiaActivity::class.java))
            Prefs.firstTime = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        /* save the visible fragment, to be retrieved in onCreate */
        if (visibleFragment != null)
            outState?.putString("visibleFragment", visibleFragment!!.javaClass.simpleName)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_OPERATION_MODE) {
            when (resultCode) {
                ModesActivity.PAPER_WALLET -> startActivity(Intent(this, PaperWalletActivity::class.java))
                ModesActivity.COLD_STORAGE -> {
                    Prefs.operationMode = "cold_storage"
                    displayFragment(WalletFragment::class.java)
                }
                ModesActivity.REMOTE_FULL_NODE -> {
                    Prefs.operationMode = "remote_full_node"
                    startActivity(Intent(this, SetupRemoteActivity::class.java))
                }
                ModesActivity.LOCAL_FULL_NODE -> {
                    displayFragment(WalletFragment::class.java)
                    if (StorageUtil.isSiadSupported) {
                        Prefs.operationMode = "local_full_node"
                    } else
                        Toast.makeText(this, "Sorry, but your device's CPU architecture is not supported by Sia's full node", Toast.LENGTH_LONG).show()
                }
            }
        }
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
            println("NEWFRAGMENT WAS NULL")
            newFragment = clazz.newInstance() as BaseFragment
            tx.add(R.id.fragment_frame, newFragment, clazz.simpleName)
        } else {
            tx.show(newFragment)
        }
        visibleFragment?.let { tx.hide(it) }
        tx.commit()
        visibleFragment = newFragment

        setTitleAndMenuFromVisibleFragment()
    }

    private fun setTitleAndMenuFromVisibleFragment() {
        if (visibleFragment == null)
            return
        supportActionBar!!.title = visibleFragment!!.javaClass.simpleName.replace("Fragment", "")
        navigationView.setCheckedItem(when (visibleFragment!!.javaClass) {
            RenterFragment::class.java -> R.id.drawer_item_renter
            HostingFragment::class.java -> R.id.drawer_item_hosting
            WalletFragment::class.java -> R.id.drawer_item_wallet
            TerminalFragment::class.java -> R.id.drawer_item_terminal
            SettingsFragment::class.java -> R.id.drawer_item_settings
            AboutFragment::class.java -> R.id.drawer_item_about
            else -> 0 /* not sure what this should actually be, if anything */
        })
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (visibleFragment?.onBackPressed() != true) {
            AlertDialog.Builder(this)
                    .setTitle("Quit?")
                    .setPositiveButton("Yes") { dialogInterface, i -> finish() }
                    .setNegativeButton("No", null)
                    .show()
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

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    companion object {
        var defaultTextColor: Int = 0
        var REQUEST_OPERATION_MODE = 2
    }
}
