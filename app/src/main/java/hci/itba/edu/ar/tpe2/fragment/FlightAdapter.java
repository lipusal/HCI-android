package hci.itba.edu.ar.tpe2.fragment;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;

/**
 * Adapter for displaying list of flights.
 */
public class FlightAdapter extends ArrayAdapter<Flight> {
    private CoordinatorLayout mCoordinatorLayout;

    FlightAdapter(Context context, List<Flight> objects, CoordinatorLayout layoutWithFAB) {
        super(context, 0, objects);
        mCoordinatorLayout = layoutWithFAB;
    }

    @Override
    public View getView(int position, View destination, ViewGroup parent) {
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