package hci.itba.edu.ar.tpe2.fragment;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;

/**
 * Adapter for displaying list of flights.
 */
public class FlightAdapter extends ArrayAdapter<Flight> {
    private CoordinatorLayout mCoordinatorLayout;
    private boolean[] statusFetched;

    FlightAdapter(Context context, List<Flight> objects, CoordinatorLayout layoutWithFAB) {
        super(context, 0, objects);
        mCoordinatorLayout = layoutWithFAB;
        statusFetched = new boolean[objects.size()];
    }

    @Override
    public View getView(final int position, View destination, final ViewGroup parent) {
        if (destination == null) {  //Item hasn't been created, inflate it from Android's default layout
            destination = LayoutInflater.from(getContext()).inflate(R.layout.activity_flights_list_item, parent, false);
        }
        final PersistentData persistentData = new PersistentData(destination.getContext());
        final List<Flight> followedFlights = persistentData.getFollowedFlights();
        final Flight flight = getItem(position);
        //Logo
        ImageView icon = (ImageView) destination.findViewById(R.id.icon);
        ImageLoader.getInstance().displayImage(flight.getAirline().getLogoURL(), icon);
        //Text
        TextView title = (TextView) destination.findViewById(R.id.text1);
        title.setText(flight.getAirline().getName() + " #" + flight.getNumber());

        //Status
        final TextView status = (TextView) destination.findViewById(R.id.status);
        if (flight.getStatus() == null) {
            status.setText("Updating...");  //TODO could cause race condition when recreating view - network request completes and then this runs
            if (statusFetched[position] == false) {
                statusFetched[position] = true;
                API.getInstance().getFlightStatus(flight.getAirline().getID(), flight.getNumber(), destination.getContext(),
                        new NetworkRequestCallback<FlightStatus>() {
                            @Override
                            public void execute(Context c, FlightStatus param) {
                                flight.setStatus(param);
                                String prettyStatus = param.toString();
                                prettyStatus = prettyStatus.substring(0, 1).toUpperCase() + prettyStatus.substring(1);
                                //Can't use #destination here, adapter recycles views. Manually find view
                                View myCoolView = parent.getChildAt(getPosition(flight) - ((ListView) parent).getFirstVisiblePosition());   //http://stackoverflow.com/questions/6766625/listview-getchildat-returning-null-for-visible-children
                                TextView myCoolText = (TextView) myCoolView.findViewById(R.id.status);
                                myCoolText.setText(prettyStatus);
                            }
                        },
                        new NetworkRequestCallback<String>() {
                            @Override
                            public void execute(Context c, String param) {
                                status.setText("Error");
                                Log.d("VOLANDO", "Couldn't get status for " + flight.toString() + ": " + param);
                                statusFetched[position] = false;    //TODO should leave this here? Will send network request again
                            }
                        });
            }
        } else {
            String prettyStatus = flight.getStatus().toString();
            prettyStatus = prettyStatus.substring(0, 1).toUpperCase() + prettyStatus.substring(1);
            status.setText(prettyStatus);
        }

        //Star
        final ImageButton star = (ImageButton) destination.findViewById(R.id.follow);
        star.setImageResource(followedFlights.contains(flight) ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
        final View finalDestination = destination;      //Need to copy to use it in inner class
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (followedFlights.contains(flight)) {
                    persistentData.removeFollowedFlight(flight, finalDestination.getContext());
                    star.setImageResource(R.drawable.ic_star_off_24dp);
                    Snackbar.make(mCoordinatorLayout == null ? v : mCoordinatorLayout, "Removed " + flight.toString(), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            persistentData.addFollowedFlight(flight, finalDestination.getContext());
                            star.setImageResource(R.drawable.ic_star_on_24dp);
                        }
                    }).show();
                } else {
                    persistentData.addFollowedFlight(flight, finalDestination.getContext());
                    star.setImageResource(R.drawable.ic_star_on_24dp);
                    Snackbar.make(mCoordinatorLayout == null ? v : mCoordinatorLayout, "Following " + flight.toString(), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            persistentData.removeFollowedFlight(flight, finalDestination.getContext());
                            star.setImageResource(R.drawable.ic_star_off_24dp);
                        }
                    }).show();
                }
            }
        });
        return destination;
    }
}