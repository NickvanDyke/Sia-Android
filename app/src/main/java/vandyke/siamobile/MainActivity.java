/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import vandyke.siamobile.backend.*;
import vandyke.siamobile.dialogs.DonateDialog;
import vandyke.siamobile.files.fragments.FilesFragment;
import vandyke.siamobile.help.fragments.HelpFragment;
import vandyke.siamobile.help.fragments.WelcomeFragment;
import vandyke.siamobile.hosting.fragments.HostingFragment;
import vandyke.siamobile.misc.LinksFragment;
import vandyke.siamobile.misc.SiaMobileApplication;
import vandyke.siamobile.misc.Utils;
import vandyke.siamobile.settings.fragments.SettingsFragment;
import vandyke.siamobile.terminal.fragments.TerminalFragment;
import vandyke.siamobile.wallet.fragments.WalletFragment;

import java.io.IOException;
import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    public static String abi;
    public static String abi32;
    public static int defaultTextColor;
    public static int backgroundColor;
    public static final String[] devAddresses = {
            "20c9ed0d1c70ab0d6f694b7795bae2190db6b31d97bc2fba8067a336ffef37aacbc0c826e5d3",
            "36ab7ac91b981f998a0f5417b7f64299375cc5ffe096841044597b48346936b49741bfeb6cf5",
            "65cc0ab13a1ccb7788cf36554daf980f162c5bf2fec9a3664192916b26c568af4eda38f666d0",
            "870878df29ee72082673ddf1e53f5ed2f52a8e84486d85e241ee531c1350066ad9622ed0ec61",
            "8a9d8e6c8d043300b967443eaaa01874efa36e69a95b03c6b970bfe5b82a7f0345424a7919af",
            "986082d52bf8a25009e7ce97508385687f3241d1e027969edbf9f63e4240cecf77bad58f40a5",
            "a58c0c63ec11b8b7b6410e76920882ea312a6762c2da98e0376ee96a8e23392f9df4e403c584",
            "a61260748a55cdbed8c28038724ada4c5284062ae799df530147aa9b8809c929145585a83cdb",
            "b05b1603c8e640a6617107d3f8f90925c13d98213822afee0c481022ef236ee9bae778ea2971",
            "ca4e94a53e257fcac10d8890aa76d73bf6a6490686301232236f8d99c4dedc1158857cf6c558",
            "f39caefc5e7f5f92a3e13a04837524a8096bc3873f551e3bd1f6c6c4cff2d2c664ddf6cfa27f"};
    public static final BigDecimal devFee = new BigDecimal("0.005"); // 0.5%

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private MenuItem activeMenuItem;
    private MenuItem selectedMenuItem;
    private boolean loadSomethingOnClose;

    private String currentFragmentTag;

    public enum Theme {
        LIGHT, DARK, AMOLED, CUSTOM
    }

    public static Theme theme;

    protected void onCreate(Bundle savedInstanceState) {
        SiaMobileApplication.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switch (SiaMobileApplication.prefs.getString("theme", "light")) {
            default:
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                setTheme(R.style.AppTheme_Light);
                theme = Theme.LIGHT;
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                setTheme(R.style.AppTheme_Light);
                theme = Theme.DARK;
                break;
            case "amoled":
                setTheme(R.style.AppTheme_Amoled);
                theme = Theme.AMOLED;
                break;
            case "custom":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                setTheme(R.style.AppTheme_Custom);
                theme = Theme.CUSTOM;
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        if (theme == Theme.CUSTOM) { // TODO: not working? just black background?
            byte[] b = Base64.decode(SiaMobileApplication.prefs.getString("customBgBase64", "null"), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (SiaMobileApplication.prefs.getBoolean("transparentBars", false)) {
            toolbar.setBackgroundColor(android.R.color.transparent);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        }

        defaultTextColor = new TextView(this).getTextColors().getDefaultColor();
        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        backgroundColor = a.data;

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (selectedMenuItem == null || !loadSomethingOnClose)
                    return;
                switch (selectedMenuItem.getItemId()) {
                    case R.id.drawer_item_files:
                        loadDrawerFragment(FilesFragment.class);
                        break;
                    case R.id.drawer_item_wallet:
                        loadDrawerFragment(WalletFragment.class);
                        break;
                    case R.id.drawer_item_hosting:
                        loadDrawerFragment(HostingFragment.class);
                        break;
                    case R.id.drawer_item_terminal:
                        loadDrawerFragment(TerminalFragment.class);
                        break;
                    case R.id.drawer_item_settings:
                        loadDrawerFragment(SettingsFragment.class);
                        break;
                    case R.id.drawer_item_links:
                        loadDrawerFragment(LinksFragment.class);
                        break;
                    case R.id.drawer_item_help:
                        loadDrawerFragment(HelpFragment.class);
                        break;
                    case R.id.drawer_item_donate:
                        DonateDialog.createAndShow(getFragmentManager());
                        break;
                }
                loadSomethingOnClose = false;
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // set action stuff for when drawer items are selected
        navigationView = (NavigationView) findViewById(R.id.drawer_navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectedMenuItem = item;
                if (selectedMenuItem == activeMenuItem) {
                    loadSomethingOnClose = false;
                    drawerLayout.closeDrawers();
                    return true;
                }

                if (item.getGroupId() != R.id.dialogs) {
                    getSupportActionBar().setTitle(item.getTitle());
                    if (activeMenuItem != null)
                        activeMenuItem.setChecked(false);
                    item.setChecked(true);
                    activeMenuItem = item;
                }
                loadSomethingOnClose = true;
                drawerLayout.closeDrawers();
                return true;
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            abi = Build.CPU_ABI;
        else
            abi = Build.SUPPORTED_ABIS[0];
        if (abi.contains("arm"))
            abi32 = "arm32";
        else if (abi.contains("x86"))
            abi32 = "x86";
        if (abi.equals("arm64-v8a"))
            abi = "arm64";
        // TODO: maybe add mips binaries

        startService(new Intent(this, CleanupService.class));
        if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("local_full_node"))
            startService(new Intent(this, SiadMonitor.class));
        else if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("cold_storage"))
            try {
                ColdStorageWallet.getInstance(this).start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        if (SiaMobileApplication.prefs.getBoolean("firstTime", true)) {
            loadDrawerFragment(WelcomeFragment.class);
            SiaMobileApplication.prefs.edit().putBoolean("firstTime", false).apply();
        } else
            switch (SiaMobileApplication.prefs.getString("startupPage", "wallet")) {
                case "files":
                    loadDrawerFragment(FilesFragment.class);
                    navigationView.setCheckedItem(R.id.drawer_item_files);
                    break;
                case "hosting":
                    loadDrawerFragment(HostingFragment.class);
                    navigationView.setCheckedItem(R.id.drawer_item_hosting);
                    break;
                case "wallet":
                    loadDrawerFragment(WalletFragment.class);
                    navigationView.setCheckedItem(R.id.drawer_item_wallet);
                    break;
                case "terminal":
                    loadDrawerFragment(TerminalFragment.class);
                    navigationView.setCheckedItem(R.id.drawer_item_terminal);
                    break;
            }
        startService(new Intent(this, WalletService.class));
    }

    public void loadDrawerFragment(Class clazz) {
        String className = clazz.getSimpleName();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Fragment currentFrag = fragmentManager.findFragmentByTag(currentFragmentTag);
        Fragment newFragment = fragmentManager.findFragmentByTag(className);

        if (currentFrag != null && currentFrag == newFragment)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        try {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if (currentFrag != null)
                transaction.hide(currentFrag);
            if (newFragment == null) {
                transaction.add(R.id.fragment_frame, (Fragment) clazz.newInstance(), className);
            } else {
                transaction.show(newFragment);
            }
            transaction.commit();
            currentFragmentTag = className;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void loadTempFragment(Fragment fragment) {
        String className = fragment.getClass().getSimpleName();
        FragmentManager fragmentManager = getFragmentManager();
        Fragment currentFrag = fragmentManager.findFragmentByTag(currentFragmentTag);
        Fragment newFragment = fragment;

//        if (currentFrag != null && currentFrag == newFragment)
//            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (currentFrag != null)
            transaction.hide(currentFrag);
        transaction.add(R.id.fragment_frame, fragment, className);
        transaction.addToBackStack(null).commit();
        currentFragmentTag = className;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    public void onDestroy() {
        super.onDestroy();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(WalletFragment.SYNC_NOTIFICATION);
        notificationManager.cancel(Siad.SIAD_NOTIFICATION);
    }

    public void copyTextView(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Sia text touch copy", ((TextView) view).getText());
        clipboard.setPrimaryClip(clip);
        Utils.snackbar(view, "Copied selection to clipboard", Snackbar.LENGTH_SHORT);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return true;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
