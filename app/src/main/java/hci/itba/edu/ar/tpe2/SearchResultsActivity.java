package hci.itba.edu.ar.tpe2;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;

public class SearchResultsActivity extends AppCompatActivity {
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
        final String from = getIntent().getStringExtra("from"),
                to = getIntent().getStringExtra("to"),
                departure = getIntent().getStringExtra("dep_date"),
                airlineID = getIntent().getStringExtra("airline_id");
        API.getInstance().searchAllFlights(from, to, departure, airlineID, this, new NetworkRequestCallback<List<Flight>>() {
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
            title.setText(flight.getAirlineName() + " #" + flight.getNumber());
//            subtitle.setText(flight.getPrettyDepartureDate());
            return destination;
        }
    }
}
