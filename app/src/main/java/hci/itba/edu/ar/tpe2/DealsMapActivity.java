package hci.itba.edu.ar.tpe2;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.data.Airport;
import hci.itba.edu.ar.tpe2.backend.data.Deal;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.data.Place;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;
import hci.itba.edu.ar.tpe2.backend.service.UpdatePriorityReceiver;

public class DealsMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String PARAM_LAST_KNOWN_LOCATION = "hci.itba.edu.ar.tpe2.DealsMapActivity.param.LAST_KNOWN_LOCATION";
    private static final String PARAM_CLOSEST_AIRPORT = "hci.itba.edu.ar.tpe2.DealsMapActivity.param.CLOSEST_AIRPORT";
    private static final String PARAM_DEALS = "hci.itba.edu.ar.tpe2.DealsMapActivity.param.DEALS";

    private GoogleMap mMap;
    private List<Deal> deals;
    private GoogleApiClient mGoogleApiClient;
    private double latitude;
    private double longitude;
    private Airport closestAirport = null;
    private static final int PERM_LOCATION = 42;
    private boolean locationPermissionGranted = false;
    private PersistentData persistentData;
    private CoordinatorLayout coordinatorLayout;
    private UpdatePriorityReceiver updatesReceiver;
    private Location lastKnownLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals_map);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.map_coordinator_layout);

        //Restore last known location and closest, if known
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PARAM_LAST_KNOWN_LOCATION)) {
                lastKnownLocation = savedInstanceState.getParcelable(PARAM_LAST_KNOWN_LOCATION);
            }
            if (savedInstanceState.containsKey(PARAM_CLOSEST_AIRPORT)) {
                closestAirport = (Airport) savedInstanceState.getSerializable(PARAM_CLOSEST_AIRPORT);
            }
            if (savedInstanceState.containsKey(PARAM_DEALS)) {
                deals = (List<Deal>) savedInstanceState.getSerializable(PARAM_DEALS);
            }
        }

        persistentData = new PersistentData(this);
        //Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                Snackbar.make(coordinatorLayout, getResources().getString(R.string.snackbar_edit), Snackbar.LENGTH_INDEFINITE).setAction(R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(closestAirport == null){
                            mMap.clear();
                            return;
                        }
                        cancelEditMap();
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                return;
                            }
                        });
                    }
                }).show();
                Collection<Airport> airports = persistentData.getAirports().values();
                for (Airport a : airports) {
                    LatLng airportPosition = new LatLng(a.getLatitude(), a.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(airportPosition)
                            .title(a.getID())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                }
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        closestAirport = persistentData.getAirports().get(marker.getTitle());
                        findDeals(closestAirport);
                        Toast.makeText(DealsMapActivity.this, getResources().getString(R.string.loading_deals) + closestAirport.toString(), Toast.LENGTH_SHORT).show();
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                return;
                            }
                        });
                    }
                });
            }
        });

        //Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(2).setChecked(true);       //Set the flights option as selected TODO I don't think this is Android standard

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                Place place = persistentData.getCities().get(marker.getTitle());
                if (place == null) {
                    place = persistentData.getAirports().get(marker.getTitle());
                }
                TextView title = (TextView) view.findViewById(R.id.title);
                title.setText(place == null ? marker.getTitle() : place.getName());   //Marker for closest airport will cause NPE, take its title directly

                TextView price = (TextView) view.findViewById(R.id.price);
                price.setText(marker.getSnippet());
                return view;
            }
        });
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onConnected(Bundle connectionHint) {
        if(lastKnownLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERM_LOCATION);
                if (!locationPermissionGranted) {
                    Toast.makeText(DealsMapActivity.this, getResources().getString(R.string.no_permissions), Toast.LENGTH_SHORT).show(); //TODO use string resource
                    return;
                }
            }
            lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            onLocationObtained();
        } else if (closestAirport != null) {
            setMarkers(deals, closestAirport);
            //TODO Facu acá modulariza cositas
        }
    }

    /**
     * Called when {@link #lastKnownLocation} is set via Google Location Services. It does <b>NOT</b>
     * guarantee that it is non-null.
     */
    private void onLocationObtained() {
        if (lastKnownLocation != null) {
            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();
            API.getInstance().getAirportsByLocation(latitude, longitude, 100, this, new NetworkRequestCallback<Airport[]>() {   //TODO use smaller radius, or let the user set it in settings
                @Override
                public void execute(Context c, Airport[] nearbyAirports) {
                    if (nearbyAirports.length == 0) {
                        Toast.makeText(DealsMapActivity.this, getResources().getString(R.string.no_airport_found), Toast.LENGTH_SHORT).show();    //TODO remove this, for debugging
                    } else if (nearbyAirports.length == 1) {
                        Toast.makeText(DealsMapActivity.this, getResources().getString(R.string.located) + nearbyAirports[0].toString(), Toast.LENGTH_SHORT).show();    //TODO remove?
                        closestAirport = nearbyAirports[0];
                        //Got closest airport, find deals for it
                        findDeals(closestAirport);
                    } else {
                        chooseClosestAirport(nearbyAirports);
                    }
                }
            });
        } else if (closestAirport != null && deals != null) {
            setMarkers(deals, closestAirport);
        } else {
            Toast.makeText(DealsMapActivity.this, getResources().getString(R.string.couldnt_locate), Toast.LENGTH_SHORT).show();    //TODO remove plz
            Log.w("VOLANDO", "Location is null");
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();     //TODO this may not be necessary on EVERY resume, check docs (also, do we need to check the user's location on EVERY app start?)
        super.onResume();
        updatesReceiver = UpdatePriorityReceiver.registerNewInstance(this, coordinatorLayout);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(updatesReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (lastKnownLocation != null) {
            outState.putParcelable(PARAM_LAST_KNOWN_LOCATION, lastKnownLocation);
        }
        if (closestAirport != null) {
            outState.putSerializable(PARAM_CLOSEST_AIRPORT, closestAirport);
        }
        if (deals != null) {
            outState.putSerializable(PARAM_DEALS, (Serializable) deals);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERM_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            onLocationObtained();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void chooseClosestAirport(final Airport[] airports) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        AlertDialog dialog = null;
        final String[] airportNames = new String[airports.length];
        for (int i = 0; i < airportNames.length; i++) {
            airportNames[i] = airports[i].getDescription();
        }
        dialogBuilder.setTitle(getResources().getString(R.string.choose_an_airport));
        dialogBuilder.setSingleChoiceItems(airportNames,0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO consider not using "accept" button and accepting the clicked option here. Can't undo this way, though


            }
        });

        dialogBuilder.setPositiveButton(getResources().getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO make API request of getDeals() and set markers and shtuff
                Airport selectedAirport = airports[((AlertDialog) dialog).getListView().getCheckedItemPosition()];
                Toast.makeText(DealsMapActivity.this, getResources().getString(R.string.loading_deals) + selectedAirport.toString(), Toast.LENGTH_SHORT).show();   //TODO remove?
                closestAirport = selectedAirport;
                findDeals(closestAirport);
            }
        });


        dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            //Deberia hacer back o dejarte ahi para que puedas hacer edit?
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * Finds deals for the specified airport and places appropriate markers, clearing any other
     * markers.
     *
     * @param origin The origin airport from which to find deals.
     */
    private void findDeals(Airport origin) {
        mMap.clear();
        LatLng airportPosition = new LatLng(origin.getLatitude(), origin.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(airportPosition));
        API.getInstance().getDeals(origin, this, new NetworkRequestCallback<Deal[]>() {
            @Override
            public void execute(Context c, Deal[] param) {
                Deal[] dealsArray = param;
                List<Deal> orderedDeals = Arrays.asList(dealsArray);
                Collections.sort(orderedDeals);
                deals = orderedDeals;
                setMarkers(deals, closestAirport);
            }},
                new NetworkRequestCallback<String>() {
                    @Override
                    public void execute(Context c, String param) {
                        Toast.makeText(DealsMapActivity.this, getResources().getString(R.string.error_api), Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void setMarkers(List<Deal> deals, Airport airport) {
        LatLng airportPosition = new LatLng(airport.getLatitude(), airport.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(airportPosition)
                .title(airport.toString())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        LatLng aux;
        float colorValue;
        for (Deal d : deals) {
            aux = new LatLng(d.getCity().getLatitude(), d.getCity().getLongitude());
            colorValue = (float) (deals.indexOf(d) * 120.0 / (deals.size() - (deals.size() == 1 ? 0 : 1)));
            mMap.addMarker(new MarkerOptions()
                    .position(aux)
                    .title(d.getCity().getID())
                    .snippet(getResources().getString(R.string.currency) + String.format("%.2f", d.getPrice()))
                    .icon(BitmapDescriptorFactory.defaultMarker(colorValue)));
        }
    }

    private void cancelEditMap() {
        mMap.clear();
        setMarkers(deals, closestAirport);
    }
}