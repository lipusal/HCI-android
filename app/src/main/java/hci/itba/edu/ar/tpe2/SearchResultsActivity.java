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
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.data.Flight;
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
    private ListView flightsList;
    private FlightAdapter flightsAdapter;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        if (savedInstanceState == null) {
            title = (TextView) findViewById(R.id.search_results_title);
            flightsList = (ListView) findViewById(R.id.flights_list);
        }
        //Search with passed parameters
        title.setText("Searching...");
        final String from = getIntent().getStringExtra(PARAM_FROM),
                to = getIntent().getStringExtra(PARAM_TO),
                departure = getIntent().getStringExtra(PARAM_DEPARTURE_DATE),
                airlineID = getIntent().getStringExtra(PARAM_AIRLINE_ID);
        API.getInstance().getAllFlights(from, to, departure, airlineID, this, new NetworkRequestCallback<List<Flight>>() {
            @Override
            public void execute(Context c, List<Flight> result) {
                flights = result;
                if (flightsAdapter == null) {
                    flightsAdapter = new FlightAdapter(SearchResultsActivity.this, flights);
                    flightsList.setAdapter(flightsAdapter);
                } else {
                    flightsAdapter.clear();
                    flightsAdapter.addAll(flights);
                    flightsAdapter.notifyDataSetChanged();
                }
                title.setText(result.size() + " " + from + "=>" + to + " flights for " + departure);
            }
        });

        //Go to flight details activity when clicking on a flight
        flightsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Flight clickedFlight = (Flight) parent.getItemAtPosition(position);
                Intent detailsIntent = new Intent(SearchResultsActivity.this, FlightDetailsActivity.class);
                detailsIntent.putExtra(FlightDetailsActivity.PARAM_FLIGHT, clickedFlight);
                startActivity(detailsIntent);
            }
        });
    }

    private class FlightAdapter extends ArrayAdapter<Flight> {

        FlightAdapter(Context context, List<Flight> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View destination, ViewGroup parent) {
            Flight flight = getItem(position);
            if (destination == null) {  //Item hasn't been created, inflate it from Android's default layout
                destination = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            //Fill in the list item with data
            TextView title = (TextView) destination.findViewById(android.R.id.text1);//,
//                    subtitle = (TextView) destination.findViewById(android.R.id.text2);
            title.setText(flight.getAirline().getName() + " #" + flight.getNumber());
//            subtitle.setText(flight.getPrettyDepartureDate());
            return destination;
        }
    }
}
