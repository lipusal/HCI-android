package hci.itba.edu.ar.tpe2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;

import hci.itba.edu.ar.tpe2.backend.FileManager;
import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;
import hci.itba.edu.ar.tpe2.backend.service.NotificationScheduler;
import hci.itba.edu.ar.tpe2.backend.service.NotificationService;
import hci.itba.edu.ar.tpe2.fragment.FlightStatusListFragment;
import hci.itba.edu.ar.tpe2.fragment.TextFragment;

public class FlightsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    private PersistentData persistentData;
    private FlightStatusListFragment flightsFragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver refreshCompleteBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            swipeRefreshLayout.setRefreshing(false);
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flights);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Go to flights search activity
                Intent i = new Intent(FlightsActivity.this, SearchActivity.class);
                startActivity(i);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);       //Set the flights option as selected TODO I don't think this is Android standard
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimary));
//                ContextCompat.getColor(this, android.R.color.holo_green_light),
//                ContextCompat.getColor(this, android.R.color.holo_orange_light),
//                ContextCompat.getColor(this, android.R.color.holo_red_light));

        //Configure the image loader GLOBALLY. Other activities can use it after this
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
            ImageLoader.getInstance().init(config);
            //TODO init this in notif service?
        }

        persistentData = new PersistentData(this);
        if (!persistentData.isInited()) {
            //TODO show loading animation
            persistentData.init(
                    new NetworkRequestCallback<Void>() {
                        @Override
                        public void execute(Context c, Void param) {
                            //TODO remove loading animation
                            //TODO refresh Your Flights list if necessary
                            if (!NotificationScheduler.areUpdatesEnabled()) {
                                Intent i = new Intent(NotificationScheduler.ACTION_UPDATE_FREQUENCY_SETTING_CHANGED);
                                sendBroadcast(i);
                            }
                        }
                    },
                    new NetworkRequestCallback<String>() {
                        @Override
                        public void execute(Context c, String param) {
                            //TODO show error and quit application, it's imperative that this not fail
                        }
                    });
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);       //Set the flights option as selected TODO I don't think this is Android standard

        //Add/refresh the flights fragment, enable/disable swipe to refresh
        List<Flight> followedFlights = persistentData.getFollowedFlights();
        if (followedFlights == null || followedFlights.isEmpty()) {
            swipeRefreshLayout.setEnabled(false);
            TextFragment textFragment = TextFragment.newInstance(getString(R.string.not_following_flights));
            if (flightsFragment == null) {    //Creating for the first time
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, textFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, textFragment).commit();
                flightsFragment = null;
            }
        } else {
            flightsFragment = FlightStatusListFragment.newInstance(new FileManager(this).loadWatchedStatuses());
            if (flightsFragment == null) {    //Creating for the first time
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, flightsFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, flightsFragment).commit();
            }

            //Override scroll behavior for swipe-to-refresh to work properly: http://stackoverflow.com/a/35779571/2333689
            getSupportFragmentManager().executePendingTransactions();   //Otherwise the view in the next line might not yet exist
            final ListView flightsFragmentListView = flightsFragment.getListView();
            flightsFragmentListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (flightsFragmentListView.getChildAt(0) != null) {
                        swipeRefreshLayout.setEnabled(flightsFragmentListView.getFirstVisiblePosition() == 0 && flightsFragmentListView.getChildAt(0).getTop() == 0);
                    }
                }
            });
        }

        //(Re-)register refresh broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshCompleteBroadcastReceiver, new IntentFilter(NotificationService.ACTION_UPDATES_COMPLETE));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshCompleteBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Flights Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://hci.itba.edu.ar.tpe2/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.flights, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent i = null;
        if (id == R.id.drawer_flights) {
            i = new Intent(this, FlightsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else if (id == R.id.drawer_search) {
            i = new Intent(this, SearchActivity.class);
        } else if (id == R.id.drawer_map) {
            i = new Intent(this, DealsMapActivity.class);
        } else if (id == R.id.drawer_settings) {
            i = new Intent(this, SettingsActivity.class);
        } else if (id == R.id.drawer_help) {

        }

        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        }
        //else, unrecognized option selected, close drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRefresh() {
        Intent intent = new Intent(this, NotificationService.class);
        intent.setAction(NotificationService.ACTION_NOTIFY_UPDATES);
        intent.putExtra(NotificationService.PARAM_BROADCAST_WHEN_COMPLETE, true);
        startService(intent);
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Flights Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://hci.itba.edu.ar.tpe2/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
