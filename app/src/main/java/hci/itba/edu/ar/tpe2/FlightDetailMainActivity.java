package hci.itba.edu.ar.tpe2;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;


import android.content.Intent;

import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.service.UpdatePriorityReceiver;
import hci.itba.edu.ar.tpe2.fragment.FlightDetailsFragment;
import hci.itba.edu.ar.tpe2.fragment.FlightDetailsMainFragment;

/**
 * Standalone activity for viewing flight details.
 */
public class FlightDetailMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FlightDetailsFragment.OnFragmentInteractionListener, FlightDetailsMainFragment.OnFragmentInteractionListener {

    public static final String PARAM_STATUS = "hci.itba.edu.ar.tpe2.FlightDetailMainActivity.STATUS";
    private Toolbar toolbar;

    FlightStatus flightStatus;
    FlightDetailsMainFragment detailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_detail_main);

        //Set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the supplied flight status
        Intent callerIntent = getIntent();
        if (!callerIntent.hasExtra(PARAM_STATUS)) {
            throw new IllegalStateException("Flight details activity started without " + PARAM_STATUS + " parameter in Intent");
        }
        flightStatus = (FlightStatus) callerIntent.getSerializableExtra(PARAM_STATUS);

        //Set the title in the toolbar
        setTitle(flightStatus.getFlight().toString());

        //Add the details fragment if necessary
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            fm.beginTransaction();
            detailsFragment = new FlightDetailsMainFragment();
            Bundle arguments = new Bundle();
            detailsFragment.setArguments(arguments);
            ft.add(R.id.fragment_container_main_details, detailsFragment);
            ft.commit();
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//    }


    @Override
    public void onFragmentInteraction(Uri uri) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.flight_detail_main, menu);

        int id = R.id.action_follow;
        if (new PersistentData(this).getWatchedStatuses().containsValue(flightStatus)) {
            toolbar.getMenu().findItem(id).setIcon(R.drawable.ic_star_white_on_24dp);
        } else {
            toolbar.getMenu().findItem(id).setIcon(R.drawable.ic_star_white_off_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_follow) {
            PersistentData persistentData = new PersistentData(this);

            if (persistentData.getWatchedStatuses().containsValue(flightStatus)) {
                persistentData.stopWatchingStatus(flightStatus);
                toolbar.getMenu().findItem(id).setIcon(R.drawable.ic_star_white_off_24dp);
            } else {
                persistentData.watchStatus(flightStatus);
                toolbar.getMenu().findItem(id).setIcon(R.drawable.ic_star_white_on_24dp);
            }
            return true;
        }
        if (id == R.id.action_review) {
            Intent reviewIntent = new Intent(this, MakeReviewActivity.class);
            reviewIntent.putExtra(FlightDetailMainActivity.PARAM_STATUS, flightStatus);
            reviewIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(reviewIntent);
            return true;
        }
        //Redraw the toolbar if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

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

    public FlightStatus getFlightStatus() {
        return flightStatus;
    }
}
