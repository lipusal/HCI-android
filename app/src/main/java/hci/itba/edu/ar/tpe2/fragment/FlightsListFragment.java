package hci.itba.edu.ar.tpe2.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.io.Serializable;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.FileManager;
import hci.itba.edu.ar.tpe2.backend.data.Flight;

/**
 * A fragment representing a list of Flights.
 * <p/><p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FlightsListFragment extends ListFragment {
    public static final String PARAM_FLIGHTS_LIST = "FLIGHTS_LIST";

    private OnFragmentInteractionListener interactionListener;
    private List<Flight> flights;

    public static FlightsListFragment newInstance(List<Flight> flights) {
        FlightsListFragment fragment = new FlightsListFragment();
        Bundle params = new Bundle();
        params.putSerializable(PARAM_FLIGHTS_LIST, (Serializable) flights);     //Cast should be safe, http://stackoverflow.com/questions/1387954/how-to-serialize-a-list-in-java
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FlightsListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        flights = new FileManager(activity).loadFollowedFlights();
        setListAdapter(new FlightAdapter(activity, flights));
        try {
            interactionListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (interactionListener != null) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
//            interactionListener.onFlightsListFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFlightsListFragmentInteraction(String id);
    }

}
