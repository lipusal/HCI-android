package hci.itba.edu.ar.tpe2;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;
import hci.itba.edu.ar.tpe2.fragment.FlightStatusListFragment;

@Deprecated
public class SearchResultsActivity extends AppCompatActivity {
    public static final String PARAM_FROM = "FROM",
            PARAM_TO = "TO",
            PARAM_DEPARTURE_DATE = "DEP_DATE",
            PARAM_AIRLINE_ID = "AIRLINE_ID";

    private static final String PARAM_FLIGHTS = "hci.itba.edu.ar.tpe2.SearchResultsActivity.PARAM_FLIGHTS";

    private List<Flight> flights;
    private boolean isDestroyed;

    //View elements
    private TextView title;
    private FlightStatusListFragment flightsFragment;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        isDestroyed = false;
        title = (TextView) findViewById(R.id.search_results_title);

        //Creating for the first time
        if (savedInstanceState == null) {
            if (flightsFragment == null) {
                flightsFragment = FlightStatusListFragment.newInstance(null);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, flightsFragment).commit();
            }
        } else {
            flightsFragment = (FlightStatusListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (savedInstanceState.containsKey(PARAM_FLIGHTS)) {
                flights = (List<Flight>) savedInstanceState.getSerializable(PARAM_FLIGHTS);
            }
        }

        if (flights == null) {
            //Do network search
            title.setText(getString(R.string.searching));
            final String from = getIntent().getStringExtra(PARAM_FROM),
                    to = getIntent().getStringExtra(PARAM_TO),
                    departure = getIntent().getStringExtra(PARAM_DEPARTURE_DATE),
                    airlineID = getIntent().getStringExtra(PARAM_AIRLINE_ID);
            searchFlights(from, to, departure, airlineID);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }

    /**
     * Adds flight obtained from search to saved instance, if present.
     *
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (flights != null) {
            savedInstanceState.putSerializable(PARAM_FLIGHTS, (Serializable) flights);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    private void searchFlights(final String from, final String to, final String departureDate, String airlineID) {
//        //Comment API code block and uncomment this one to get hardcoded flights (i.e. Interwebz not working)  TODO borrar para entrega final
//        Gson gson = new Gson();
//        JsonObject flightsJson = gson.fromJson("{\"meta\":{\"uuid\":\"f75a8a2c-1e31-41eb-b1e7-536083ff3411\",\"time\":\"20437.355ms\"},\"page\":1,\"page_size\":30,\"total\":5,\"currency\":{\"id\":\"USD\"},\"flights\":[{\"price\":{\"adults\":{\"base_fare\":739,\"quantity\":1},\"children\":null,\"infants\":null,\"total\":{\"charges\":36.95,\"taxes\":125.63,\"fare\":739,\"total\":901.58}},\"outbound_routes\":[{\"segments\":[{\"arrival\":{\"date\":\"2016-10-03 10:04:00\",\"airport\":{\"id\":\"LAX\",\"description\":\"Aeropuerto Internacional Los Angeles, Los Angeles, Estados Unidos\",\"time_zone\":\"-08:00\",\"city\":{\"id\":\"LAX\",\"name\":\"Los Angeles, California, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"departure\":{\"date\":\"2016-10-03 08:10:00\",\"airport\":{\"id\":\"JFK\",\"description\":\"Aeropuerto Internacional John F Kennedy, Nueva York, Estados Unidos\",\"time_zone\":\"-05:00\",\"city\":{\"id\":\"JFK\",\"name\":\"Nueva York, New York, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"id\":125885,\"number\":4704,\"cabin_type\":\"ECONOMY\",\"airline\":{\"id\":\"CM\",\"name\":\"Copa\",\"rating\":5.4},\"duration\":\"04:54\",\"stopovers\":[]}],\"duration\":\"04:54\"}]},{\"price\":{\"adults\":{\"base_fare\":497,\"quantity\":1},\"children\":null,\"infants\":null,\"total\":{\"charges\":34.79,\"taxes\":74.55,\"fare\":497,\"total\":606.34}},\"outbound_routes\":[{\"segments\":[{\"arrival\":{\"date\":\"2016-10-03 17:39:00\",\"airport\":{\"id\":\"LAX\",\"description\":\"Aeropuerto Internacional Los Angeles, Los Angeles, Estados Unidos\",\"time_zone\":\"-08:00\",\"city\":{\"id\":\"LAX\",\"name\":\"Los Angeles, California, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"departure\":{\"date\":\"2016-10-03 15:30:00\",\"airport\":{\"id\":\"JFK\",\"description\":\"Aeropuerto Internacional John F Kennedy, Nueva York, Estados Unidos\",\"time_zone\":\"-05:00\",\"city\":{\"id\":\"JFK\",\"name\":\"Nueva York, New York, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"id\":125888,\"number\":4845,\"cabin_type\":\"ECONOMY\",\"airline\":{\"id\":\"AM\",\"name\":\"Aeromexico\",\"rating\":6},\"duration\":\"05:09\",\"stopovers\":[]}],\"duration\":\"05:09\"}]},{\"price\":{\"adults\":{\"base_fare\":785,\"quantity\":1},\"children\":null,\"infants\":null,\"total\":{\"charges\":54.95,\"taxes\":117.75,\"fare\":785,\"total\":957.7}},\"outbound_routes\":[{\"segments\":[{\"arrival\":{\"date\":\"2016-10-03 07:48:00\",\"airport\":{\"id\":\"LAX\",\"description\":\"Aeropuerto Internacional Los Angeles, Los Angeles, Estados Unidos\",\"time_zone\":\"-08:00\",\"city\":{\"id\":\"LAX\",\"name\":\"Los Angeles, California, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"departure\":{\"date\":\"2016-10-03 05:40:00\",\"airport\":{\"id\":\"JFK\",\"description\":\"Aeropuerto Internacional John F Kennedy, Nueva York, Estados Unidos\",\"time_zone\":\"-05:00\",\"city\":{\"id\":\"JFK\",\"name\":\"Nueva York, New York, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"id\":125891,\"number\":9075,\"cabin_type\":\"ECONOMY\",\"airline\":{\"id\":\"AM\",\"name\":\"Aeromexico\",\"rating\":6},\"duration\":\"05:08\",\"stopovers\":[]}],\"duration\":\"05:08\"}]},{\"price\":{\"adults\":{\"base_fare\":669,\"quantity\":1},\"children\":null,\"infants\":null,\"total\":{\"charges\":20.07,\"taxes\":100.35,\"fare\":669,\"total\":789.42}},\"outbound_routes\":[{\"segments\":[{\"arrival\":{\"date\":\"2016-10-03 03:39:00\",\"airport\":{\"id\":\"LAX\",\"description\":\"Aeropuerto Internacional Los Angeles, Los Angeles, Estados Unidos\",\"time_zone\":\"-08:00\",\"city\":{\"id\":\"LAX\",\"name\":\"Los Angeles, California, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"departure\":{\"date\":\"2016-10-03 02:00:00\",\"airport\":{\"id\":\"JFK\",\"description\":\"Aeropuerto Internacional John F Kennedy, Nueva York, Estados Unidos\",\"time_zone\":\"-05:00\",\"city\":{\"id\":\"JFK\",\"name\":\"Nueva York, New York, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"id\":125894,\"number\":1281,\"cabin_type\":\"ECONOMY\",\"airline\":{\"id\":\"AZ\",\"name\":\"Alitalia\",\"rating\":6},\"duration\":\"04:39\",\"stopovers\":[]}],\"duration\":\"04:39\"}]},{\"price\":{\"adults\":{\"base_fare\":736,\"quantity\":1},\"children\":null,\"infants\":null,\"total\":{\"charges\":14.72,\"taxes\":139.84,\"fare\":736,\"total\":890.56}},\"outbound_routes\":[{\"segments\":[{\"arrival\":{\"date\":\"2016-10-03 13:32:00\",\"airport\":{\"id\":\"LAX\",\"description\":\"Aeropuerto Internacional Los Angeles, Los Angeles, Estados Unidos\",\"time_zone\":\"-08:00\",\"city\":{\"id\":\"LAX\",\"name\":\"Los Angeles, California, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"departure\":{\"date\":\"2016-10-03 11:50:00\",\"airport\":{\"id\":\"JFK\",\"description\":\"Aeropuerto Internacional John F Kennedy, Nueva York, Estados Unidos\",\"time_zone\":\"-05:00\",\"city\":{\"id\":\"JFK\",\"name\":\"Nueva York, New York, Estados Unidos\",\"country\":{\"id\":\"US\",\"name\":\"Estados Unidos\"}}}},\"id\":125897,\"number\":9732,\"cabin_type\":\"ECONOMY\",\"airline\":{\"id\":\"IB\",\"name\":\"Iberia\",\"rating\":6.15},\"duration\":\"04:42\",\"stopovers\":[]}],\"duration\":\"04:42\"}]}],\"filters\":[{\"key\":\"airline\",\"values\":[{\"id\":\"CM\",\"name\":\"Copa\",\"logo\":\"http://eiffel.itba.edu.ar/hci/service4/images/CM.png\",\"count\":1},{\"id\":\"AZ\",\"name\":\"Alitalia\",\"logo\":\"http://eiffel.itba.edu.ar/hci/service4/images/AZ.png\",\"count\":1},{\"id\":\"AM\",\"name\":\"Aeromexico\",\"logo\":\"http://eiffel.itba.edu.ar/hci/service4/images/AM.png\",\"count\":2},{\"id\":\"IB\",\"name\":\"Iberia\",\"logo\":\"http://eiffel.itba.edu.ar/hci/service4/images/IB.png\",\"count\":1}]},{\"key\":\"stopover\",\"values\":[{\"id\":0,\"count\":5}]},{\"key\":\"price\",\"min\":606.34,\"max\":957.7}]}", JsonObject.class);
//        List<Flight> flights = new ArrayList<>();
//        for (JsonElement flight : flightsJson.getAsJsonArray("flights")) {
//            flights.add(Flight.fromJson(flight.getAsJsonObject()));
//        }
//        if(!isDestroyed) {
//            FlightStatusListFragment newFragment = FlightStatusListFragment.newInstance(SearchResultsActivity.this.flights);
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFragment).commit();
//            title.setText(flights.size() + " " + from + "=>" + to + " flights for " + departureDate);
//        }


        //TODO cancel all network requests onDestroy()
        API.getInstance().getAllFlights(from, to, departureDate, airlineID, this,
                new NetworkRequestCallback<List<Flight>>() {
                    @Override
                    public void execute(Context c, List<Flight> result) {
                        SearchResultsActivity.this.flights = result;
                        if (isDestroyed) {    //e.g. rotated screen before network request completed.
                            return;
                        }
                        if (result.isEmpty()) {
                            title.setText(getString(R.string.err_no_flights_found));
                        } else {
                            //TODO won't need this when deleting this activity
//                            FlightStatusListFragment newFragment = FlightStatusListFragment.newInstance(SearchResultsActivity.this.flights);
//                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFragment).commit();
                            title.setText(result.size() + " " + from + "=>" + to + " flights for " + departureDate);
                        }
                    }
                },
                new NetworkRequestCallback<String>() {
                    @Override
                    public void execute(Context c, String param) {
                        if (!isDestroyed) {
                            title.setText("Network error, couldn't find flights =(");
                        }
                    }
                });

    }
}
