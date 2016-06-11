package hci.itba.edu.ar.tpe2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.data.Airport;
import hci.itba.edu.ar.tpe2.backend.data.Deal;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;

public class DealsMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private Deal[] deals;
    private GoogleApiClient mGoogleApiClient;
    private double latitude;
    private double longitude;
    private Airport closestAirport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals_map);
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
        mMap = googleMap;

        API.getInstance().getDeals("BUE", this, new NetworkRequestCallback<Deal[]>() {
            @Override
            public void execute(Context c, Deal[] param) {
                deals = param;
                List<Deal> orderedDeals = Arrays.asList(deals);
                Collections.sort(orderedDeals);
                LatLng aux;
                float average = 0;
                int i = 0;
                for (Deal d : deals) {
                    average += d.getPrice();
                    i++;
                }
                average = average / i;
                float colorValue;
                for (Deal d : deals) {
                    aux = new LatLng(d.getCity().getLatitude(), d.getCity().getLongitude());
                    colorValue = (float) (orderedDeals.indexOf(d) * 120.0 / (orderedDeals.size() - (orderedDeals.size() == 1 ? 0 : 1)));
                    mMap.addMarker(new MarkerOptions().position(aux).title(d.toString()).icon(BitmapDescriptorFactory.defaultMarker(colorValue)));
                }
            }
        });

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Facu, lee esto para pedirle permisos al usuario si no los dio y qué hacer si no da permiso:
            // Consider calling ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastKnownLocation != null) {
            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();
            API.getInstance().getAirportsByLocation(latitude, longitude, 10, this, new NetworkRequestCallback<Airport[]>() {
                @Override
                public void execute(Context c, Airport[] nearbyAirports) {
                    if (nearbyAirports.length == 0) {
                        Toast.makeText(DealsMapActivity.this, "No airport found near you =(", Toast.LENGTH_SHORT).show();    //TODO remove this, for debugging
                    } else {
                        Toast.makeText(DealsMapActivity.this, "Located you at " + nearbyAirports[0].toString(), Toast.LENGTH_SHORT).show();    //TODO remove?
                        closestAirport = nearbyAirports[0]; //TODO Discutir cómo devolvemos el mas cercano y demás
                        LatLng airportPosition = new LatLng(closestAirport.getLatitude(), closestAirport.getLongitude());
                        //Add special marker in the found airport
                        //TODO consider using a bigger, different marker rather than a differently-colored one (or use a contrasting color, see Material Design color guidelines)
                        mMap.addMarker(new MarkerOptions().position(airportPosition).title("Marker in" + closestAirport.getCity().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                        //Move the camera to it
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(airportPosition));
                    }
                }
            });
        } else {
            Toast.makeText(DealsMapActivity.this, "WHER U AT BOI I CAN'T FIND U", Toast.LENGTH_SHORT).show();    //TODO remove plz
            Log.w("VOLANDO", "Location is null");
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        //TODO wat do jier?
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO wat do jier?
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();     //TODO this may not be necessary on EVERY resume, check docs (also, do we need to check the user's location on EVERY app start?)
        super.onResume();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
