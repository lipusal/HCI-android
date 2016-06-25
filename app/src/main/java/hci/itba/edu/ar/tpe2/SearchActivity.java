package hci.itba.edu.ar.tpe2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.data.Airline;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.APIRequest;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;
import hci.itba.edu.ar.tpe2.backend.service.UpdatePriorityReceiver;

public class SearchActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private EditText flightField;
    private AutoCompleteTextView airlineField;
    private Button searchButton;
    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    private UpdatePriorityReceiver updatesReceiver;

    private PersistentData persistentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Creating for the first time
        if (savedInstanceState == null) {
        }

        //Set up autocomplete airline field
        persistentData = new PersistentData(this);
        List<Airline> airlines = new ArrayList<>(persistentData.getAirlines().values());
        String[] airlineIDs = new String[airlines.size()];
        for (int i = 0; i < airlineIDs.length; i++) {
            airlineIDs[i] = airlines.get(i).getID();
        }
        airlineField = (AutoCompleteTextView) findViewById(R.id.airline_id);
        airlineField.setAdapter(new AirlineAdapter(this, airlineIDs));
//        airlineField.setThreshold(1);

        flightField = (EditText) findViewById(R.id.flight_number);
        searchButton = (Button) findViewById(R.id.search_button);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_search);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    final ProgressDialog progressDialog = new ProgressDialog(SearchActivity.this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setTitle(getString(R.string.searching) + "...");
                    progressDialog.show();

                    searchButton.setEnabled(false);
                    searchButton.setText(R.string.searching);
                    //Search and go if found
                    String airlineID = airlineField.getText().toString();
                    int flightNumber = Integer.parseInt(flightField.getText().toString());
                    API.getInstance().getFlightStatus(
                            airlineID,
                            flightNumber,
                            SearchActivity.this,
                            new NetworkRequestCallback<FlightStatus>() {
                                @Override
                                public void execute(Context context, FlightStatus fetchedStatus) {
                                    progressDialog.dismiss();
                                    Intent searchIntent = new Intent(SearchActivity.this, FlightDetailMainActivity.class);
                                    searchIntent.putExtra(FlightDetailMainActivity.PARAM_STATUS, fetchedStatus);
                                    startActivity(searchIntent);
                                }
                            },
                            new NetworkRequestCallback<String>() {
                                @Override
                                public void execute(Context c, String param) {
                                    String msg;
                                    if(param.equals(APIRequest.ERR_STRING)) {
                                        msg = getString(R.string.err_network);
                                    }
                                    else {
                                        msg = getString(R.string.err_no_flights_found);
                                    }
                                    progressDialog.dismiss();
                                    Toast.makeText(SearchActivity.this, msg, Toast.LENGTH_LONG).show();
                                    searchButton.setEnabled(true);
                                    searchButton.setText(R.string.search);
                                }
                            });

                }
            }
        });

        //Set up the drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);       //Set the search option as selected TODO I don't think this is Android standard
    }

    @Override
    public void onResume() {
        super.onResume();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);       //Set the flights option as selected TODO I don't think this is Android standard

        searchButton.setEnabled(true);
        searchButton.setText(R.string.search);

        updatesReceiver = UpdatePriorityReceiver.registerNewInstance(this, coordinatorLayout);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(updatesReceiver);
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
        // Inflate the menu; this adds items toField the action bar if it is present.
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

    private boolean validateFields() {
        boolean valid = true;
        String airline = airlineField.getText().toString(),
                flight = flightField.getText().toString();
        if (airline.isEmpty()) {
            airlineField.setError(getString(R.string.err_empty_airline_id));
            valid = false;
        } else if (false) {    //TODO handle invalid input

        }
        if (flight.isEmpty()) {
            flightField.setError(getString(R.string.err_empty_flight_number));
            valid = false;
        } else if (false) {    //TODO handle invalid input

        }


        return valid;
    }

    /**
     * Adapter to fill the autocomplete Airlines drop-down
     */
    private class AirlineAdapter extends ArrayAdapter<String> {
        public AirlineAdapter(Context context, String[] objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(final int position, View destinationView, final ViewGroup parent) {
            if (destinationView == null) {  //Item hasn't been created, inflate it from Android's default layout
                destinationView = LayoutInflater.from(SearchActivity.this).inflate(R.layout.list_item_airline, parent, false);
            }
            Airline airline = persistentData.getAirlines().get(getItem(position));

            ImageView logo = (ImageView) destinationView.findViewById(R.id.logo);
            logo.setImageDrawable(getDrawable(airline.getDrawableLogoID()));

            TextView text = (TextView) destinationView.findViewById(R.id.text);
            text.setText(airline.getID() + " - " + airline.getName());

            return destinationView;
        }
    }
}
