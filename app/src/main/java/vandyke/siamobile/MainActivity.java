package vandyke.siamobile;

import android.app.FragmentTransaction;
import android.content.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdView;
import vandyke.siamobile.dialogs.RemoveAdsFeesDialog;
import vandyke.siamobile.fragments.*;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences prefs;
    public static RequestQueue requestQueue;
    public static MainActivity instance;
    public static int defaultTextColor;
    public static int backgroundColor;
    public static boolean darkMode;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private MenuItem activeMenuItem;

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("darkModeEnabled", false)) {
            setTheme(R.style.AppThemeDark);
            darkMode = true;
        } else {
            setTheme(R.style.AppTheme);
            darkMode = false;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        requestQueue = Volley.newRequestQueue(this);
        instance = this;
        defaultTextColor = new TextView(this).getTextColors().getDefaultColor();
        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        backgroundColor = a.data;

        // disabled for now because it's annoying. TODO: uncomment before release
//        if (prefs.getBoolean("adsEnabled", true)) {
//            MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
//            ((AdView)findViewById(R.id.adView)).loadAd(new AdRequest.Builder().build());
//        } else
        ((AdView)findViewById(R.id.adView)).setVisibility(View.GONE);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set up drawer button on action bar
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View drawerView) {
                // TODO: maybe make it so it waits until drawer close if fragment doesn't already exist, but loads immediately if it does?
                super.onDrawerClosed(drawerView);
                if (activeMenuItem == null)
                    return;
                switch (activeMenuItem.getItemId()) {
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
                    case R.id.drawer_item_about:
                        // TODO: about stuff
                        break;
                    case R.id.drawer_item_remove_ads_fees:
                        RemoveAdsFeesDialog.createAndShow(getFragmentManager());
                        break;
                    case R.id.drawer_item_donate:
                        // TODO: donate stuff
                        break;
                }
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // set action stuff for when drawer items are selected
        navigationView = (NavigationView)findViewById(R.id.drawer_navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item == activeMenuItem) {
                    drawerLayout.closeDrawers();
                    return true;
                }

                if (item.getGroupId() != R.id.money_stuff) {
                    if (activeMenuItem != null)
                        activeMenuItem.setChecked(false);
                    item.setChecked(true);
                }
                activeMenuItem = item;
                drawerLayout.closeDrawers();
                return true;
            }
        });

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                SharedPreferences.Editor editor = prefs.edit();
                switch (key) {
                    case "operationMode":
                        if (prefs.getString("operationMode", "remote_full_node").equals("remote_full_node")) {
                            editor.putString("address", prefs.getString("remoteAddress", "192.168.1.11:9980"));
                        } else if (prefs.getString("operationMode", "remote_full_node").equals("local_wallet_and_server")) {
                            editor.putString("address", "localhost:9980");
                        }
                        editor.apply();
                        break;
                    case "remoteAddress":
                        if (prefs.getString("operationMode", "remote_full_node").equals("remote_full_node")) {
                            editor.putString("address", prefs.getString("remoteAddress", "192.168.1.11:9980"));
                            editor.apply();
                        }
                        break;
                    case "darkModeEnabled":
                        if (prefs.getBoolean("darkModeEnabled", false)) {
                            setTheme(R.style.AppThemeDark);
                        } else {
                            setTheme(R.style.AppTheme);
                        }
                        // restart to apply the theme; don't need to change darkMode boolean since app is restarting
                        finish();
                        Intent intent = new Intent(MainActivity.instance, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addCategory("darkModeRestart");
                        startActivity(intent);
                        break;
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        if (getIntent().hasCategory("darkModeRestart"))
            loadDrawerFragment(SettingsFragment.class);
    }

    public void loadDrawerFragment(Class clazz) {
        // TODO: might be able to use replace here instead of showing and hiding. might be better way to do this. also maybe limit size of backstack
        String className = clazz.getSimpleName();
        FragmentManager fragmentManager = getFragmentManager();

//        Fragment currentFrag = fragmentManager.findFragmentById(R.id.fragment_frame);
//
//        Fragment newFragment = fragmentManager.findFragmentByTag(className);
//        if (newFragment == null) {
//            try {
//                if (currentFrag != null)
//                    fragmentManager.beginTransaction().hide(currentFrag)
//                            .add(R.id.fragment_frame, (Fragment)clazz.newInstance(), className)
//                            .addToBackStack(null).commit();
//                else
//                    fragmentManager.beginTransaction().add(R.id.fragment_frame, (Fragment)clazz.newInstance(), className).commit();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        } else {
//            if (currentFrag != null)
//                fragmentManager.beginTransaction().hide(currentFrag).show(newFragment).addToBackStack(null).commit();
//            else
//                fragmentManager.beginTransaction().show(newFragment).addToBackStack(null).commit();
//        }
        try {
            fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.fragment_frame, (Fragment)clazz.newInstance(), className).commit();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
//        getSupportActionBar().setTitle(className.replace("Fragment", ""));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    public void copyTextView(View view) {
        ClipboardManager clipboard = (ClipboardManager)MainActivity.instance.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Sia text touch copy", ((TextView)view).getText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied selection to clipboard", Toast.LENGTH_SHORT).show();
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
}
