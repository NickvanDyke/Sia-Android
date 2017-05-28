package vandyke.sia;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import vandyke.sia.fragments.FilesFragment;
import vandyke.sia.fragments.TerminalFragment;
import vandyke.sia.fragments.WalletFragment;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private MenuItem activeMenuItem;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        // set toolbar as actionbar
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        // set new TerminalFragment in the frame meant for displaying fragments
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, new TerminalFragment()).commit();

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        // set up drawer button on action bar
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // set action stuff for when drawer items are selected
        navigationView = (NavigationView)findViewById(R.id.drawer_navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (activeMenuItem != null)
                    activeMenuItem.setChecked(false);
                item.setChecked(true);
                activeMenuItem = item;
                drawerLayout.closeDrawers();

                switch (item.getItemId()) {
                    case R.id.drawer_item_files:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, new FilesFragment()).commit();
                        getSupportActionBar().setTitle("Files");
                        break;
                    case R.id.drawer_item_wallet:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, new WalletFragment()).commit();
                        getSupportActionBar().setTitle("Wallet");
                        break;
                    case R.id.drawer_item_terminal:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, new TerminalFragment()).commit();
                        getSupportActionBar().setTitle("Terminal");
                        break;
                    case R.id.drawer_item_settings:
                        // settings fragment
                        break;
                }
                return false;
            }
        });
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
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }
}
