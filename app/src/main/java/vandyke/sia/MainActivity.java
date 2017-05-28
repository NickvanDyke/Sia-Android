package vandyke.sia;

import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import vandyke.sia.Drawer.DrawerAdapter;
import vandyke.sia.Drawer.DrawerItem;
import vandyke.sia.fragments.FilesFragment;
import vandyke.sia.fragments.TerminalFragment;
import vandyke.sia.fragments.WalletFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView drawerList;

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
        // load the items in the array that the drawer's adapter will use
        ArrayList<DrawerItem> drawerItems = new ArrayList<>();
        drawerItems.add(new DrawerItem("Files", getResources().getDrawable(R.drawable.cloud_icon)));
        drawerItems.add(new DrawerItem("Wallet", getResources().getDrawable(R.drawable.wallet_icon)));
        drawerItems.add(new DrawerItem("Terminal", getResources().getDrawable(R.drawable.terminal_icon)));
        drawerItems.add(new DrawerItem("Settings", getResources().getDrawable(R.drawable.settings_icon)));
        // create the drawer's adapter and set it
        drawerList = (ListView)findViewById(R.id.left_drawer_list);
        drawerList.setAdapter(new DrawerAdapter(this, R.layout.drawer_list_item, drawerItems));
        // set action stuff for when drawer items are selected
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, new FilesFragment()).commit();
                        getSupportActionBar().setTitle("Files");
                        break;
                    case 1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, new WalletFragment()).commit();
                        getSupportActionBar().setTitle("Wallet");
                        break;
                    case 2:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, new TerminalFragment()).commit();
                        getSupportActionBar().setTitle("Terminal");
                        break;
                    case 3:
                        // settings fragment
                        break;
                }
                drawerList.setItemChecked(position, true);
//                drawerLayout.closeDrawer(drawerList);
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
        getMenuInflater().inflate(R.menu.terminal_toolbar, menu);
        return true;
    }
}
