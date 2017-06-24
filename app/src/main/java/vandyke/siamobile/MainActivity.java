package vandyke.siamobile;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdView;
import vandyke.siamobile.dialogs.RemoveAdsFeesDialog;
import vandyke.siamobile.fragments.*;

import java.io.*;
import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    public static String abi;
    public static SharedPreferences prefs;
    public static RequestQueue requestQueue;
    public static MainActivity instance;
    public static int defaultTextColor;
    public static int backgroundColor;
    public static final String[] devAddresses = {"986082d52bf8a25009e7ce97508385687f3241d1e027969edbf9f63e4240cecf77bad58f40a5",
            "8a9d8e6c8d043300b967443eaaa01874efa36e69a95b03c6b970bfe5b82a7f0345424a7919af",
            "65cc0ab13a1ccb7788cf36554daf980f162c5bf2fec9a3664192916b26c568af4eda38f666d0"};
    public static final BigDecimal devFee = new BigDecimal("0.005"); // 0.5%

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private MenuItem activeMenuItem;
    private MenuItem selectedMenuItem;
    private boolean loadSomethingOnClose;


    private String currentFragmentTag;
    private static final int SELECT_PICTURE = 1;

    public enum Theme {
        LIGHT, DARK, AMOLED, CUSTOM
    }

    public static Theme theme;

    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switch (prefs.getString("theme", "light")) {
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
        if (theme == Theme.CUSTOM) {
            byte[] b = Base64.decode(prefs.getString("customBgBase64", "null"), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            getWindow().setBackgroundDrawable(new BitmapDrawable(bitmap));
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (prefs.getBoolean("transparentBars", false)) {
            toolbar.setBackgroundColor(android.R.color.transparent);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        }

        defaultTextColor = new TextView(this).getTextColors().getDefaultColor();
        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        backgroundColor = a.data;

        requestQueue = Volley.newRequestQueue(this);
        instance = this;
        // disabled for now because it's annoying. TODO: uncomment before release
//        if (prefs.getBoolean("adsEnabled", true)) {
//            MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
//            ((AdView)findViewById(R.id.adView)).loadAd(new AdRequest.Builder().build());
//        } else
        ((AdView) findViewById(R.id.adView)).setVisibility(View.GONE);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View drawerView) {
                // TODO: maybe make it so it waits until drawer close if fragment doesn't already exist, but loads immediately if it does?
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
                    case R.id.drawer_item_about:
                        loadDrawerFragment(AboutFragment.class);
                        break;
                    case R.id.drawer_item_help:
                        loadDrawerFragment(HelpFragment.class);
                        break;
                    case R.id.drawer_item_remove_ads_fees:
                        RemoveAdsFeesDialog.createAndShow(getFragmentManager());
                        break;
//                    case R.id.drawer_item_donate:
//                        // TODO: donate stuff
//                        break;
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
        if (abi.equals("arm64-v8a"))
            abi = "aarch64";
        // TODO: maybe add mips64 binary


        if (prefs.getString("operationMode", "remote_full_node").equals("local_full_node"))
            Siad.getInstance().start();

        if (getIntent().hasCategory("settings")) {
            loadDrawerFragment(SettingsFragment.class);
            navigationView.setCheckedItem(R.id.drawer_item_settings);
        } else
            switch (prefs.getString("startupPage", "wallet")) {
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
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageURI = data.getData();
                InputStream input = null;
                try {
                    input = getContentResolver().openInputStream(selectedImageURI);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putString("customBgBase64", Base64.encodeToString(b, Base64.DEFAULT));
                prefsEditor.apply();
                restartAndLaunch("settings");
            }
        }
    }

    public void restartAndLaunch(String category) {
        finish();
        Intent intent = new Intent(MainActivity.instance, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(category);
        startActivity(intent);
    }

    public void loadDrawerFragment(Class clazz) {
        String className = clazz.getSimpleName();
        FragmentManager fragmentManager = getFragmentManager();
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
         NotificationManager notificationManager = (NotificationManager)MainActivity.instance.getSystemService(Context.NOTIFICATION_SERVICE);
         notificationManager.cancel(WalletFragment.SYNC_NOTIFICATION);
         notificationManager.cancel(Siad.SIAD_NOTIFICATION);
     }

    public static AlertDialog.Builder getDialogBuilder() {
        switch (MainActivity.theme) {
            case LIGHT:
                return new AlertDialog.Builder(instance);
            case DARK:
                return new AlertDialog.Builder(instance);
            case AMOLED:
                return new AlertDialog.Builder(instance, R.style.DialogTheme_Amoled);
            case CUSTOM:
                return new AlertDialog.Builder(instance, R.style.DialogTheme_Custom);
            default:
                return new AlertDialog.Builder(instance);
        }
    }

    public void copyTextView(View view) {
        ClipboardManager clipboard = (ClipboardManager) MainActivity.instance.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Sia text touch copy", ((TextView) view).getText());
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

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    // will return null if the abi is an unsupported one and therefore there is not a binary for it
    public static File copyBinary(String filename) {
        try {
            InputStream in = instance.getAssets().open(filename + "-" + abi);
            File result = new File(instance.getFilesDir(), filename + "-" + abi);
            if (result.exists())
                return result;
            FileOutputStream out = new FileOutputStream(result);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
            result.setExecutable(true);
            in.close();
            out.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getWorkingDirectory() {
        File result;
        if (prefs.getBoolean("useExternal", false)) {
            result = instance.getExternalFilesDir(null);
            if (result == null) { // external storage not found
                Toast.makeText(instance, "No external storage found. Using internal", Toast.LENGTH_LONG).show();
                result = instance.getFilesDir();
            }
        } else
            result = instance.getFilesDir();
        return result;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static String externalStorageStateDescription() {
        switch (Environment.getExternalStorageState()) {
            case Environment.MEDIA_BAD_REMOVAL:
                return "external storage was previously removed before being unmounted";
            case Environment.MEDIA_CHECKING:
                return "external storage is present but being disk-checked";
            case Environment.MEDIA_EJECTING:
                return "external storage is in the process of ejecting";
            case Environment.MEDIA_MOUNTED:
                return "external storage is present and mounted with read/write access";
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                return "external storage is present but mounted as read-only";
            case Environment.MEDIA_NOFS:
                return "external storage is present but is blank or using an unsupported filesystem";
            case Environment.MEDIA_REMOVED:
                return "external storage is not present";
            case Environment.MEDIA_SHARED:
                return "external storage is present but being shared via USB";
            case Environment.MEDIA_UNKNOWN:
                return "external storage is in an unknown state";
            case Environment.MEDIA_UNMOUNTABLE:
                return "external storage is present but cannot be mounted. May be corrupted";
            case Environment.MEDIA_UNMOUNTED:
                return "external storage is present but unmounted";
            default:
                return "external storage state missed all cases";
        }
    }
}
