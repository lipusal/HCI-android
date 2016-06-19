package hci.itba.edu.ar.tpe2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hci.itba.edu.ar.tpe2.backend.FileManager;
import hci.itba.edu.ar.tpe2.backend.data.Airline;
import hci.itba.edu.ar.tpe2.backend.data.Airport;
import hci.itba.edu.ar.tpe2.backend.data.City;
import hci.itba.edu.ar.tpe2.backend.data.Country;
import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;
import hci.itba.edu.ar.tpe2.backend.service.NotificationScheduler;
import hci.itba.edu.ar.tpe2.backend.service.NotificationService;
import hci.itba.edu.ar.tpe2.fragment.FlightsListFragment;
import hci.itba.edu.ar.tpe2.fragment.TextFragment;

public class FlightsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    private FlightsListFragment flightsFragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver refreshCompleteBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            swipeRefreshLayout.setRefreshing(false);
        }
    };

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

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimary));
//                ContextCompat.getColor(this, android.R.color.holo_green_light),
//                ContextCompat.getColor(this, android.R.color.holo_orange_light),
//                ContextCompat.getColor(this, android.R.color.holo_red_light));

        if (!NotificationScheduler.areUpdatesEnabled()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            NotificationScheduler.setUpdateFrequency(this, Long.parseLong(preferences.getString(getString(R.string.pref_key_update_frequency), "-1")));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);       //Set the flights option as selected TODO I don't think this is Android standard

        //Add/refresh the flights fragment, enable/disable swipe to refresh
        List<Flight> followedFlights = PersistentData.getInstance().getFollowedFlights();
        if (followedFlights.isEmpty()) {
            swipeRefreshLayout.setEnabled(false);
            TextFragment textFragment = TextFragment.newInstance(getString(R.string.not_following_flights));
            if (flightsFragment == null) {    //Creating for the first time
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, textFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, textFragment).commit();
                flightsFragment = null;
            }
//            getSupportFragmentManager().executePendingTransactions();
//            textFragment.getTextView().setCompoundDrawablesWithIntrinsicBounds(null, null, null, getDrawable(R.drawable.ic_flight));
        } else {
//            swipeRefreshLayout.setEnabled(true);
            flightsFragment = FlightsListFragment.newInstance(new FileManager(this).loadFollowedFlights());
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
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshCompleteBroadcastReceiver, new IntentFilter(NotificationService.FILTER_UPDATES_COMPLETE));
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
        init();
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

        if(i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        }
        //else, unrecognized option selected, close drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Initializes necessary data. Some data depends on previous data (i.e. cities have countries
     * inside, airports have cities inside) so it needs to be loaded asynchronously but one after
     * the other, hence the ugly nesting.
     */
    private void init() {
        final PersistentData data = PersistentData.getInstance();
        final FileManager fileManager = new FileManager(this);
        //No persistent data stored in files, download from network
        if (fileManager.loadCountries().length == 0) {  //Load countries FIRST, see method documentation
            Log.w("VOLANDO", "Querying API for countries and cities and airports");
            API.getInstance().getAllCountries(FlightsActivity.this, new NetworkRequestCallback<Country[]>() {
                @Override
                public void execute(Context c, Country[] countries) {
                    Map<String, Country> la = new HashMap<>(countries.length);
                    for(Country country : countries) {
                        la.put(country.getID(), country);
                    }
                    if (fileManager.saveCountries(countries)) {
                        Log.d("VOLANDO", countries.length + " countries saved from network.");
                        data.setCountries(la);
                        /**
                         * Once done saving countries, get cities, setting their country to the
                         * COMPLETE Country object (API returns incomplete objects for this method)
                         */
                        API.getInstance().getAllCities(FlightsActivity.this, new NetworkRequestCallback<City[]>() {
                            @Override
                            public void execute(Context c, City[] cities) {
                                Map<String, City> la = new HashMap<>(cities.length);
                                for(City city : cities) {
                                    //City has an incomplete Country object stored. Replace it with the complete one.
                                    city.setCountry(data.getCountries().get(city.getCountry().getID()));
                                    la.put(city.getID(), city);
                                }
                                if (fileManager.saveCities(cities)) {
                                    Log.d("VOLANDO", cities.length + " cities loaded from network.");
                                    data.setCities(la);
                                    /**
                                     * Once done saving cities, get airports, setting their city and country to the
                                     * COMPLETE objects
                                     */
                                    API.getInstance().getAllAirports(FlightsActivity.this, new NetworkRequestCallback<Airport[]>() {
                                        @Override
                                        public void execute(Context c, Airport[] airports) {
                                            Map<String, Airport> la = new HashMap<>(airports.length);
                                            for (Airport airport : airports) {
                                                //Airport has an incomplete City object stored. Replace it with the complete one.
                                                airport.setCity(data.getCities().get(airport.getCity().getID()));
                                                la.put(airport.getID(), airport);
                                            }
                                            if (fileManager.saveAirports(airports)) {
                                                Log.d("VOLANDO", airports.length + " airports loaded from network.");
                                                data.setAirports(la);
                                            } else {
                                                Log.w("VOLANDO", "Couldn't save airports.");
                                            }
                                        }
                                    });
                                }
                                else {
                                    Log.w("VOLANDO", "Couldn't save cities.");
                                }
                            }
                        });
                    }
                    else {
                        Log.w("VOLANDO", "Couldn't save countries.");
                    }
                }
            });
        } else {  //Persistent data found, load from file
            //Countries
            Country[] countries = fileManager.loadCountries();
            Map<String, Country> countriesMap = new HashMap<>(countries.length);
            for (Country c : countries) {
                countriesMap.put(c.getID(), c);
            }
            data.setCountries(countriesMap);
            //Cities
            City[] cities = fileManager.loadCities();
            Map<String, City> citiesMap = new HashMap<>(cities.length);
            for (City c : cities) {
                citiesMap.put(c.getID(), c);
            }
            data.setCities(citiesMap);
            //Airports
            Airport[] airports = fileManager.loadAirports();
            Map<String, Airport> airportsMap = new HashMap<>(airports.length);
            for (Airport a : airports) {
                airportsMap.put(a.getID(), a);
            }
            data.setAirports(airportsMap);
            //Airlines
            Airline[] airlines = fileManager.loadAirlines();
            Map<String, Airline> airlinesMap = new HashMap<>(airlines.length);
            for (Airline a : airlines) {
                airlinesMap.put(a.getID(), a);
            }
            data.setAirlines(airlinesMap);
            Log.d("VOLANDO", "Loaded " + countries.length + " countries, " + cities.length + " cities, " + airports.length + " airports and " + airlines.length + " airlines from local storage.");
        }
        //Load airlines
        if (fileManager.loadAirlines().length == 0) {
            Log.w("VOLANDO", "Querying API for airlines");
            API.getInstance().getAllAirlines(this, new NetworkRequestCallback<Airline[]>() {
                @Override
                public void execute(Context c, Airline[] airlines) {
                    Map<String, Airline> la = new HashMap<>(airlines.length);
                    for (Airline airline : airlines) {
                        la.put(airline.getID(), airline);
                        System.out.println(airline.toString());
                    }
                    if (fileManager.saveAirlines(airlines)) {
                        Log.d("VOLANDO", airlines.length + " airlines loaded from network.");
                        data.setAirlines(la);
                    } else {
                        Log.w("VOLANDO", "Couldn't save airlines.");
                    }
                }
            });
        }
        if (data.getFollowedFlights() == null) {
            data.setFollowedFlights(fileManager.loadFollowedFlights());
            Log.d("VOLANDO", "Loaded " + data.getFollowedFlights().size() + " followed flights.");
        } else {
            Log.d("VOLANDO", data.getFollowedFlights().size() + " flights saved in persistent data.");
        }
        //Configure the image loader GLOBALLY. Other activities can use it after this
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
            ImageLoader.getInstance().init(config);
        }
    }

    @Override
    public void onRefresh() {
        Intent intent = new Intent(this, NotificationService.class);
        intent.setAction(NotificationService.ACTION_NOTIFY_UPDATES);
        intent.putExtra(NotificationService.PARAM_BROADCAST_WHEN_COMPLETE, true);
        startService(intent);
        swipeRefreshLayout.setRefreshing(true);
        //TODO disallow this when there are no flights
    }
}
