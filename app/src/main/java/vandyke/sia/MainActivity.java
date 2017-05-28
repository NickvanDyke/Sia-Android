package vandyke.sia;

import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import vandyke.sia.fragments.TerminalFragment;

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
        drawerItems.add(new DrawerItem("Files", getResources().getDrawable(android.R.drawable.btn_star)));
        drawerItems.add(new DrawerItem("Wallet", getResources().getDrawable(android.R.drawable.btn_star)));
        drawerItems.add(new DrawerItem("Terminal", getResources().getDrawable(android.R.drawable.btn_star)));
        // create the drawer's adapter and set it
        drawerList = (ListView)findViewById(R.id.left_drawer_list);
        drawerList.setAdapter(new DrawerAdapter(this, R.layout.drawer_list_item, drawerItems));
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
