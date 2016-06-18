package hci.itba.edu.ar.tpe2.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import hci.itba.edu.ar.tpe2.FlightDetailsActivity;
import hci.itba.edu.ar.tpe2.FlightsActivity;
import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.Flight;

/**
 * A fragment representing a list of Flights.
 * <p/><p/>
 * Activities containing this fragment MAY implement the {@link OnFragmentInteractionListener}
 * interface. If not implemented, fragment falls back to standard behavior defined in
 * {@link DefaultInteractionHandler}.
 */
public class FlightsListFragment extends ListFragment {
    public static final String PARAM_FLIGHTS_LIST = "hci.itba.edu.ar.tpe2.fragment.FlightsListFragment.FLIGHTS_LIST";

    private OnFragmentInteractionListener interactionListener;
    private List<Flight> flights;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FlightsListFragment() {}

    public static FlightsListFragment newInstance(List<Flight> flights) {
        FlightsListFragment result = new FlightsListFragment();
        if(flights != null) {
            Bundle params = new Bundle();
            params.putSerializable(PARAM_FLIGHTS_LIST, (Serializable) flights);
            result.setArguments(params);
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(PARAM_FLIGHTS_LIST)) {
            flights = (List<Flight>) getArguments().getSerializable(PARAM_FLIGHTS_LIST);
        }
        else {
            if (flights == null) {
                flights = Collections.EMPTY_LIST;
            }
        }
        setListAdapter(new FlightAdapter(getActivity(), flights, (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout)));
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            interactionListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
            interactionListener = new DefaultInteractionHandler();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        interactionListener.onFlightClicked((Flight) listView.getItemAtPosition(position));
    }

    /**
     * If you want to override interaction behavior, implement this interface.
     */
    public interface OnFragmentInteractionListener {

        /**
         * Called when a flight is clicked. Default behavior is to start the {@link FlightDetailsActivity}
         * with the clicked Flight.
         *
         * @param clickedFlight The clicked flight.
         */
        void onFlightClicked(Flight clickedFlight);
    }

    private class DefaultInteractionHandler implements OnFragmentInteractionListener {

        @Override
        public void onFlightClicked(Flight clickedFlight) {
            Intent detailsIntent = new Intent(getActivity(), FlightDetailsActivity.class);
            detailsIntent.putExtra(FlightDetailsActivity.PARAM_FLIGHT, clickedFlight);
            startActivity(detailsIntent);
        }
    }

}
