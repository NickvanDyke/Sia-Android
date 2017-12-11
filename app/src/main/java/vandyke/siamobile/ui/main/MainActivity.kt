/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.main

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
import android.view.WindowManager
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import vandyke.siamobile.R
import vandyke.siamobile.data.local.Prefs
import vandyke.siamobile.data.siad.SiadService
import vandyke.siamobile.ui.BaseFragment
import vandyke.siamobile.ui.about.AboutFragment
import vandyke.siamobile.ui.about.SetupRemoteActivity
import vandyke.siamobile.ui.hosting.fragments.HostingFragment
import vandyke.siamobile.ui.renter.view.RenterFragment
import vandyke.siamobile.ui.settings.ModesActivity
import vandyke.siamobile.ui.settings.SettingsFragment
import vandyke.siamobile.ui.terminal.TerminalFragment
import vandyke.siamobile.ui.wallet.view.PaperWalletActivity
import vandyke.siamobile.ui.wallet.view.WalletFragment
import vandyke.siamobile.util.observe

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var visibleFragment: BaseFragment? = null
    private var loadingDialog: SiadLoadingDialog? = null
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Prefs.operationMode == "local_full_node")
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
        if (Prefs.transparentBars) {
            toolbar.setBackgroundColor(resources.getColor(android.R.color.transparent))
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }

        defaultTextColor = TextView(this).currentTextColor


        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel.visibleFragmentClass.observe(this) {
            displayFragment(it)
        }

        viewModel.siadIsLoading.observe(this) {
            if (it) {
                loadingDialog = SiadLoadingDialog()
                loadingDialog!!.show(supportFragmentManager, "loading dialog")
            } else {
                loadingDialog?.dismiss()
            }
        }

        /* notify view model when navigation items are selected */
        navigationView.setNavigationItemSelectedListener({ item ->
            drawerLayout.closeDrawers()
            viewModel.navigationItemSelected(item)
            return@setNavigationItemSelectedListener true
        })
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)

        /* set the VM's visibleFragmentClass differently depending on whether the activity is being recreated */
        if (savedInstanceState == null) {
            viewModel.visibleFragmentClass.value = when (Prefs.startupPage) {
                "renter" -> RenterFragment::class.java
                "hosting" -> HostingFragment::class.java
                "wallet" -> WalletFragment::class.java
                "terminal" -> TerminalFragment::class.java
                else -> throw Exception()
            }
        } else {
            /* find the fragment currently visible stored in the savedInstanceState */
            val storedFragmentClass = supportFragmentManager.findFragmentByTag(savedInstanceState.getString("visibleFragment")).javaClass
            viewModel.visibleFragmentClass.value = storedFragmentClass
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
        visibleFragment?.let { tx.hide(it) }
        tx.commit()
        visibleFragment = newFragment

        setTitleAndMenuFromVisibleFragment()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_OPERATION_MODE) {
            when (resultCode) {
                ModesActivity.PAPER_WALLET -> startActivity(Intent(this, PaperWalletActivity::class.java))
                ModesActivity.REMOTE_FULL_NODE -> {
                    Prefs.operationMode = "remote_full_node"
                    startActivity(Intent(this, SetupRemoteActivity::class.java))
                }
                ModesActivity.LOCAL_FULL_NODE -> {
                    viewModel.visibleFragmentClass.value = WalletFragment::class.java
                    Prefs.operationMode = "local_full_node"
                }
            }
        }
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
            else -> throw Exception() /* not sure what this should actually be, if anything */
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
