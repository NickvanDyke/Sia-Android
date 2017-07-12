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
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import vandyke.siamobile.about.AboutActivity;
import vandyke.siamobile.backend.CleanupService;
import vandyke.siamobile.backend.GlobalPrefsListener;
import vandyke.siamobile.backend.WalletMonitorService;
import vandyke.siamobile.backend.coldstorage.ColdStorageService;
import vandyke.siamobile.backend.siad.SiadMonitorService;
import vandyke.siamobile.dialogs.DonateDialog;
import vandyke.siamobile.files.fragments.FilesFragment;
import vandyke.siamobile.help.ModesActivity;
import vandyke.siamobile.help.fragments.FragmentSetupRemote;
import vandyke.siamobile.hosting.fragments.HostingFragment;
import vandyke.siamobile.misc.LinksFragment;
import vandyke.siamobile.misc.Utils;
import vandyke.siamobile.settings.fragments.SettingsFragment;
import vandyke.siamobile.terminal.fragments.TerminalFragment;
import vandyke.siamobile.wallet.fragments.WalletCreateFragment;
import vandyke.siamobile.wallet.fragments.WalletFragment;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    public static int backgroundColor;
    public static int REQUEST_MODE = 2;

    private GlobalPrefsListener globalPrefsListener;

    @BindView(R.id.drawer_layout)
    public DrawerLayout drawerLayout;
    @BindView(R.id.drawer_navigation_view)
    public NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    private Fragment currentlyVisibleFragment;
    private Stack<String> titleBackstack;
    private Stack<Integer> menuItemBackstack;
    private int backstackCount;

    public enum Theme {
        LIGHT, DARK, AMOLED, CUSTOM
    }

    public static Theme theme;

    protected void onCreate(Bundle savedInstanceState) {
        SiaMobileApplication.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        globalPrefsListener = new GlobalPrefsListener(this);
        SiaMobileApplication.prefs.registerOnSharedPreferenceChangeListener(globalPrefsListener);
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
        ButterKnife.bind(this);
        if (theme == Theme.CUSTOM) {
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

        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        backgroundColor = a.data;

        titleBackstack = new Stack<>();
        menuItemBackstack = new Stack<>();
        backstackCount = 0;
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                if (getFragmentManager().getBackStackEntryCount() < backstackCount) {
                    titleBackstack.pop();
                    menuItemBackstack.pop();
                    setTitle(titleBackstack.peek());
                    Integer menuItemId = menuItemBackstack.peek();
                    if (menuItemId != null)
                        navigationView.setCheckedItem(menuItemId);
                    backstackCount--;
                    currentlyVisibleFragment = getFragmentManager().findFragmentById(R.id.fragment_frame);
                } else {
                    backstackCount++;
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // set action stuff for when drawer items are selected
        NavigationView.OnNavigationItemSelectedListener drawerListener = new NavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                int menuItemId = item.getItemId();
                switch (menuItemId) {
                    case R.id.drawer_item_files:
                        displayFragment(FilesFragment.class, "Files", menuItemId);
                        return true;
                    case R.id.drawer_item_hosting:
                        displayFragment(HostingFragment.class, "Hosting", menuItemId);
                        return true;
                    case R.id.drawer_item_wallet:
                        displayFragment(WalletFragment.class, "Wallet", menuItemId);
                        return true;
                    case R.id.drawer_item_terminal:
                        displayFragment(TerminalFragment.class, "Terminal", menuItemId);
                        return true;
                    case R.id.drawer_item_settings:
                        displayFragment(SettingsFragment.class, "Settings", menuItemId);
                        return true;
                    case R.id.drawer_item_about:
                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        return false;
                    case R.id.drawer_item_links:
                        displayFragment(LinksFragment.class, "Links", menuItemId);
                        return true;
                    case R.id.drawer_item_help:
                        startActivityForResult(new Intent(MainActivity.this, ModesActivity.class), REQUEST_MODE);
                        return false;
                    case R.id.drawer_item_donate:
                        DonateDialog.createAndShow(getFragmentManager());
                        return false;
                }
                return true;
            }
        };
        navigationView.setNavigationItemSelectedListener(drawerListener);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        startService(new Intent(this, CleanupService.class));
        if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("local_full_node"))
            startService(new Intent(this, SiadMonitorService.class));
        else if (SiaMobileApplication.prefs.getString("operationMode", "cold_storage").equals("cold_storage"))
            startService(new Intent(this, ColdStorageService.class));

        startService(new Intent(this, WalletMonitorService.class));

        switch (SiaMobileApplication.prefs.getString("startupPage", "wallet")) {
            case "files":
                displayFragment(FilesFragment.class, "Files", R.id.drawer_item_files);
                break;
            case "hosting":
                displayFragment(HostingFragment.class, "Hosting", R.id.drawer_item_hosting);
                break;
            case "wallet":
                displayFragment(WalletFragment.class, "Wallet", R.id.drawer_item_wallet);
                break;
            case "terminal":
                displayFragment(TerminalFragment.class, "Terminal", R.id.drawer_item_terminal);
                break;
        }

        if (SiaMobileApplication.prefs.getBoolean("firstTime", true)) {
            startActivityForResult(new Intent(this, ModesActivity.class), REQUEST_MODE);
            startActivity(new Intent(this, AboutActivity.class));
            SiaMobileApplication.prefs.edit().putBoolean("firstTime", false).apply();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MODE) {
            if (resultCode == ModesActivity.COLD_STORAGE) {
                SiaMobileApplication.prefs.edit().putString("operationMode", "cold_storage").apply();
                displayFragment(WalletFragment.class, "Wallet", R.id.drawer_item_wallet);
                if (currentlyVisibleFragment instanceof WalletFragment)
                    ((WalletFragment) currentlyVisibleFragment).replaceExpandFrame(new WalletCreateFragment());
            } else if (resultCode == ModesActivity.REMOTE_FULL_NODE) {
                SiaMobileApplication.prefs.edit().putString("operationMode", "remote_full_node").apply();
                displayFragment(FragmentSetupRemote.class, "Remote setup", null);
            } else if (resultCode == ModesActivity.LOCAL_FULL_NODE) {
                displayFragment(WalletFragment.class, "Wallet", R.id.drawer_item_wallet);
                if (Utils.isSiadSupported()) {
                    SiaMobileApplication.prefs.edit().putString("operationMode", "local_full_node").apply();
                    if (currentlyVisibleFragment instanceof WalletFragment)
                        ((WalletFragment) currentlyVisibleFragment).replaceExpandFrame(new WalletCreateFragment());
                } else
                    Toast.makeText(this, "Sorry, but your device's CPU architecture is not supported by Sia's full node", Toast.LENGTH_LONG).show();
                displayFragment(WalletFragment.class, "Wallet", R.id.drawer_item_wallet);
            }
        }
    }

    public void displayFragment(Class clazz, String title, Integer menuItemId) {
        String className = clazz.getSimpleName();
        FragmentManager fragmentManager = getFragmentManager();
        Fragment newFragment = fragmentManager.findFragmentByTag(className);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if ((newFragment != null && newFragment.isVisible())
        || (currentlyVisibleFragment != null && currentlyVisibleFragment == newFragment))
            return;

        if (currentlyVisibleFragment != null) {
            transaction.addToBackStack(null);
            transaction.hide(currentlyVisibleFragment);
        }

        if (newFragment == null) {
            try {
                newFragment = (Fragment) clazz.newInstance();
                transaction.add(R.id.fragment_frame, newFragment, className);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            transaction.show(newFragment);
        }
        setTitle(title);
        transaction.commit();
        currentlyVisibleFragment = newFragment;
        titleBackstack.push(title);
        menuItemBackstack.push(menuItemId);
        if (menuItemId != null)
            navigationView.setCheckedItem(menuItemId);
    }

    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else if (titleBackstack.size() <= 1) {
            Utils.getDialogBuilder(this)
                    .setTitle("Quit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    public void copyTextView(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Sia text touch copy", ((TextView) view).getText());
        clipboard.setPrimaryClip(clip);
        Utils.snackbar(view, "Copied selection to clipboard", Snackbar.LENGTH_SHORT);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
