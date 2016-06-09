package hci.itba.edu.ar.tpe2;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

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

import hci.itba.edu.ar.tpe2.backend.data.Deal;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;

public class DealsMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Deal[] deals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng buenosAires = new LatLng(-34, -58);
        mMap.addMarker(new MarkerOptions().position(buenosAires).title("Marker in Buenos Aires").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(buenosAires));
        API.getInstance().getDeals("BUE", this, new NetworkRequestCallback<Deal[]>() {
            @Override
            public void execute(Context c, Deal[] param) {
                deals = param;
                List<Deal> orderedDeals = Arrays.asList(deals);
                Collections.sort(orderedDeals);
                LatLng aux;
                float average = 0;
                int i = 0;
                for(Deal d : deals){
                    average += d.getPrice();
                    i++;
                }
                average = average / i;
                float colorValue;
                for(Deal d : deals){
                    aux = new LatLng(d.getCity().getLatitude(),d.getCity().getLongitude());
                    colorValue = (float) (orderedDeals.indexOf(d)*120.0/(orderedDeals.size()-(orderedDeals.size()==1?0:1)));
                    mMap.addMarker(new MarkerOptions().position(aux).title(d.toString()).icon(BitmapDescriptorFactory.defaultMarker(colorValue)));
                }
            }
        });

    }
}
