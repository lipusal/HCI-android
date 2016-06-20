package hci.itba.edu.ar.tpe2.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import hci.itba.edu.ar.tpe2.FlightDetailMainActivity;
import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;

/**
 * A fragment representing a list of flight statuses.
 * <p/><p/>
 * Activities containing this fragment MAY implement the {@link OnFragmentInteractionListener}
 * interface. If not implemented, fragment falls back to standard behavior defined in
 * {@link DefaultInteractionHandler}.
 */
public class FlightStatusListFragment extends ListFragment {
    public static final String PARAM_STATUS_LIST = "hci.itba.edu.ar.tpe2.fragment.FlightStatusListFragment.STATUS_LIST";

    private OnFragmentInteractionListener interactionListener;
    private List<FlightStatus> statuses;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FlightStatusListFragment() {
    }

    public static FlightStatusListFragment newInstance(List<FlightStatus> statuses) {
        FlightStatusListFragment result = new FlightStatusListFragment();
        if (statuses != null) {
            Bundle params = new Bundle();
            params.putSerializable(PARAM_STATUS_LIST, (Serializable) statuses);
            result.setArguments(params);
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(PARAM_STATUS_LIST)) {
            statuses = (List<FlightStatus>) getArguments().getSerializable(PARAM_STATUS_LIST);
        } else {
            if (statuses == null) {
                statuses = Collections.EMPTY_LIST;
            }
        }
        setListAdapter(new FlightAdapter(getActivity(), statuses, (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout)));
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
        interactionListener.onFlightClicked((FlightStatus) listView.getItemAtPosition(position));
    }

    /**
     * If you want to override interaction behavior, implement this interface.
     */
    public interface OnFragmentInteractionListener {

        /**
         * Called when a flight status is clicked. Default behavior is to start the
         * {@link FlightDetailMainActivity} with the clicked status.
         *
         * @param clickedStatus The clicked status.
         */
        void onFlightClicked(FlightStatus clickedStatus);
    }

    private class DefaultInteractionHandler implements OnFragmentInteractionListener {

        @Override
        public void onFlightClicked(FlightStatus clickedStatus) {
            Intent detailsIntent = new Intent(getActivity(), FlightDetailMainActivity.class);
            detailsIntent.putExtra(FlightDetailMainActivity.PARAM_FLIGHT, clickedStatus);           //TODO finalize contract to send flight status or params to search a flight status
            Log.d("VOLANDO", "Would start details activity for " + clickedStatus.getFlight().toString());
//            startActivity(detailsIntent); //TODO uncomment
        }
    }

}
