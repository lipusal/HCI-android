package hci.itba.edu.ar.tpe2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;

public class SearchResultsActivity extends AppCompatActivity {
    public static final String PARAM_FROM = "FROM",
            PARAM_TO = "TO",
            PARAM_DEPARTURE_DATE = "DEP_DATE",
            PARAM_AIRLINE_ID = "AIRLINE_ID";

    private String from, to, airlineId;
    private Date departure;
    private List<Flight> flights;

    //View elements
    private ListView flightsListView;
    private FlightAdapter flightsAdapter;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        if (savedInstanceState == null) {
            title = (TextView) findViewById(R.id.search_results_title);
            flightsListView = (ListView) findViewById(R.id.flights_list);
        }
        //Search with passed parameters
        title.setText("Searching...");
        final String from = getIntent().getStringExtra(PARAM_FROM),
                to = getIntent().getStringExtra(PARAM_TO),
                departure = getIntent().getStringExtra(PARAM_DEPARTURE_DATE),
                airlineID = getIntent().getStringExtra(PARAM_AIRLINE_ID);
//        API.getInstance().getAllFlights(from, to, departure, airlineID, this, new NetworkRequestCallback<List<Flight>>() {
//                    @Override
//                    public void execute(Context c, List<Flight> result) {
//                        flights = result;
//                        if (flightsAdapter == null) {
//                            flightsAdapter = new FlightAdapter(SearchResultsActivity.this, flights);
//                            flightsListView.setAdapter(flightsAdapter);
//                        } else {
//                            flightsAdapter.clear();
//                            flightsAdapter.addAll(flights);
//                            flightsAdapter.notifyDataSetChanged();
//                        }
//                        title.setText(result.size() + " " + from + "=>" + to + " flights for " + departure);
//                    }
//                },
//                new NetworkRequestCallback<String>() {
//                    @Override
//                    public void execute(Context c, String param) {
//                        title.setText("Network error, couldn't find flights =(");
//                    }
//                });

        //Comment previous API code block and uncomment this one to get hardcoded flights (i.e. Interwebz not working)  TODO borrar para entrega final
        Gson gson = new Gson();
        JsonObject flightsJson = gson.fromJson("{\"meta\":{\"uuid\":\"f75a8a2c-1e31-41eb-b1e7-536083ff3411\",\"time\":\"20437.355ms\"},\"page\":1,\"page_size\":30,\"total\":5,\"currency\":{\"id\":\"USD\"},\"flights\":[{\"price\":{\"adults\":{\"base_fare\":739,\"quantity\":1},\"children\":null,\"infants\":null,\"total\":{\"charges\":36.95,\"taxes\":125.63,\"fare\":739,\"total\":901.58}},\"outbound_routes\":[{\"segments\":[{\"arrival\":{\"date\":\"2016-10-03 10:04:00\",\"airport\":{\"id\":\"LAX\",\"description\":\"Aeropuerto Internacional Los Angeles, Los Angeles, Estados Unidos\",\"time_zone\":\"-08:00\",\"city\":{\"id\":\"LAX\",\"name\":\"Los Angeles, California, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"departure\":{\"date\":\"2016-10-03 08:10:00\",\"airport\":{\"id\":\"JFK\",\"description\":\"Aeropuerto Internacional John F Kennedy, Nueva York, Estados Unidos\",\"time_zone\":\"-05:00\",\"city\":{\"id\":\"JFK\",\"name\":\"Nueva York, New York, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"id\":125885,\"number\":4704,\"cabin_type\":\"ECONOMY\",\"airline\":{\"id\":\"CM\",\"name\":\"Copa\",\"rating\":5.4},\"duration\":\"04:54\",\"stopovers\":[]}],\"duration\":\"04:54\"}]},{\"price\":{\"adults\":{\"base_fare\":497,\"quantity\":1},\"children\":null,\"infants\":null,\"total\":{\"charges\":34.79,\"taxes\":74.55,\"fare\":497,\"total\":606.34}},\"outbound_routes\":[{\"segments\":[{\"arrival\":{\"date\":\"2016-10-03 17:39:00\",\"airport\":{\"id\":\"LAX\",\"description\":\"Aeropuerto Internacional Los Angeles, Los Angeles, Estados Unidos\",\"time_zone\":\"-08:00\",\"city\":{\"id\":\"LAX\",\"name\":\"Los Angeles, California, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"departure\":{\"date\":\"2016-10-03 15:30:00\",\"airport\":{\"id\":\"JFK\",\"description\":\"Aeropuerto Internacional John F Kennedy, Nueva York, Estados Unidos\",\"time_zone\":\"-05:00\",\"city\":{\"id\":\"JFK\",\"name\":\"Nueva York, New York, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"id\":125888,\"number\":4845,\"cabin_type\":\"ECONOMY\",\"airline\":{\"id\":\"AM\",\"name\":\"Aeromexico\",\"rating\":6},\"duration\":\"05:09\",\"stopovers\":[]}],\"duration\":\"05:09\"}]},{\"price\":{\"adults\":{\"base_fare\":785,\"quantity\":1},\"children\":null,\"infants\":null,\"total\":{\"charges\":54.95,\"taxes\":117.75,\"fare\":785,\"total\":957.7}},\"outbound_routes\":[{\"segments\":[{\"arrival\":{\"date\":\"2016-10-03 07:48:00\",\"airport\":{\"id\":\"LAX\",\"description\":\"Aeropuerto Internacional Los Angeles, Los Angeles, Estados Unidos\",\"time_zone\":\"-08:00\",\"city\":{\"id\":\"LAX\",\"name\":\"Los Angeles, California, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"departure\":{\"date\":\"2016-10-03 05:40:00\",\"airport\":{\"id\":\"JFK\",\"description\":\"Aeropuerto Internacional John F Kennedy, Nueva York, Estados Unidos\",\"time_zone\":\"-05:00\",\"city\":{\"id\":\"JFK\",\"name\":\"Nueva York, New York, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"id\":125891,\"number\":9075,\"cabin_type\":\"ECONOMY\",\"airline\":{\"id\":\"AM\",\"name\":\"Aeromexico\",\"rating\":6},\"duration\":\"05:08\",\"stopovers\":[]}],\"duration\":\"05:08\"}]},{\"price\":{\"adults\":{\"base_fare\":669,\"quantity\":1},\"children\":null,\"infants\":null,\"total\":{\"charges\":20.07,\"taxes\":100.35,\"fare\":669,\"total\":789.42}},\"outbound_routes\":[{\"segments\":[{\"arrival\":{\"date\":\"2016-10-03 03:39:00\",\"airport\":{\"id\":\"LAX\",\"description\":\"Aeropuerto Internacional Los Angeles, Los Angeles, Estados Unidos\",\"time_zone\":\"-08:00\",\"city\":{\"id\":\"LAX\",\"name\":\"Los Angeles, California, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"departure\":{\"date\":\"2016-10-03 02:00:00\",\"airport\":{\"id\":\"JFK\",\"description\":\"Aeropuerto Internacional John F Kennedy, Nueva York, Estados Unidos\",\"time_zone\":\"-05:00\",\"city\":{\"id\":\"JFK\",\"name\":\"Nueva York, New York, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"id\":125894,\"number\":1281,\"cabin_type\":\"ECONOMY\",\"airline\":{\"id\":\"AZ\",\"name\":\"Alitalia\",\"rating\":6},\"duration\":\"04:39\",\"stopovers\":[]}],\"duration\":\"04:39\"}]},{\"price\":{\"adults\":{\"base_fare\":736,\"quantity\":1},\"children\":null,\"infants\":null,\"total\":{\"charges\":14.72,\"taxes\":139.84,\"fare\":736,\"total\":890.56}},\"outbound_routes\":[{\"segments\":[{\"arrival\":{\"date\":\"2016-10-03 13:32:00\",\"airport\":{\"id\":\"LAX\",\"description\":\"Aeropuerto Internacional Los Angeles, Los Angeles, Estados Unidos\",\"time_zone\":\"-08:00\",\"city\":{\"id\":\"LAX\",\"name\":\"Los Angeles, California, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"departure\":{\"date\":\"2016-10-03 11:50:00\",\"airport\":{\"id\":\"JFK\",\"description\":\"Aeropuerto Internacional John F Kennedy, Nueva York, Estados Unidos\",\"time_zone\":\"-05:00\",\"city\":{\"id\":\"JFK\",\"name\":\"Nueva York, New York, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"id\":125897,\"number\":9732,\"cabin_type\":\"ECONOMY\",\"airline\":{\"id\":\"IB\",\"name\":\"Iberia\",\"rating\":6.15},\"duration\":\"04:42\",\"stopovers\":[]}],\"duration\":\"04:42\"}]}],\"filters\":[{\"key\":\"airline\",\"values\":[{\"id\":\"CM\",\"name\":\"Copa\",\"logo\":\"http://eiffel.itba.edu.ar/hci/service4/images/CM.png\",\"count\":1},{\"id\":\"AZ\",\"name\":\"Alitalia\",\"logo\":\"http://eiffel.itba.edu.ar/hci/service4/images/AZ.png\",\"count\":1},{\"id\":\"AM\",\"name\":\"Aeromexico\",\"logo\":\"http://eiffel.itba.edu.ar/hci/service4/images/AM.png\",\"count\":2},{\"id\":\"IB\",\"name\":\"Iberia\",\"logo\":\"http://eiffel.itba.edu.ar/hci/service4/images/IB.png\",\"count\":1}]},{\"key\":\"stopover\",\"values\":[{\"id\":0,\"count\":5}]},{\"key\":\"price\",\"min\":606.34,\"max\":957.7}]}", JsonObject.class);
        List<Flight> flights = new ArrayList<>();
        for (JsonElement flight : flightsJson.getAsJsonArray("flights")) {
            flights.add(Flight.fromJson(flight.getAsJsonObject()));
        }
        flightsAdapter = new FlightAdapter(SearchResultsActivity.this, flights);
        flightsListView.setAdapter(flightsAdapter);

        //Go to flight details activity when clicking on a flight
        flightsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Flight clickedFlight = (Flight) parent.getItemAtPosition(position);
                Intent detailsIntent = new Intent(SearchResultsActivity.this, FlightDetailMainActivity.class);
                detailsIntent.putExtra(FlightDetailMainActivity.PARAM_FLIGHT, clickedFlight);
                startActivity(detailsIntent);
            }
        });
    }

    /**
     * Adapter for populating lists with flights for the user to un/follow and see details of.
     */
    private class FlightAdapter extends ArrayAdapter<Flight> {

        FlightAdapter(Context context, List<Flight> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View destination, ViewGroup parent) {
            final List<Flight> followedFlights = PersistentData.getInstance().getFollowedFlights();
            final Flight flight = getItem(position);
            if (destination == null) {  //Item hasn't been created, inflate it from Android's default layout
                destination = LayoutInflater.from(getContext()).inflate(R.layout.activity_flights_list_item, parent, false);
            }
            //Logo
            ImageView icon = (ImageView) destination.findViewById(R.id.icon);
            ImageLoader.getInstance().displayImage(flight.getAirline().getLogoURL(), icon);
            //Text
            TextView title = (TextView) destination.findViewById(R.id.text1);
            title.setText(flight.getAirline().getName() + " #" + flight.getNumber());
            //Star
            final ImageButton star = (ImageButton) destination.findViewById(R.id.follow);
            star.setImageResource(followedFlights.contains(flight) ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
            final View finalDestination = destination;      //Need to copy to use it in inner class
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (followedFlights.contains(flight)) {
                        PersistentData.getInstance().removeFollowedFlight(flight, finalDestination.getContext());
                        star.setImageResource(R.drawable.ic_star_off_24dp);
                    } else {
                        PersistentData.getInstance().addFollowedFlight(flight, finalDestination.getContext());
                        star.setImageResource(R.drawable.ic_star_on_24dp);
                    }
                }
            });
            return destination;
        }
    }
}
