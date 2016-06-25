package hci.itba.edu.ar.tpe2.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import hci.itba.edu.ar.tpe2.FlightDetailMainActivity;
import hci.itba.edu.ar.tpe2.FlightsActivity;
import hci.itba.edu.ar.tpe2.R;
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
    private View lastClickedView;

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
        setListAdapter(new FlightStatusAdapter(getActivity(), statuses, (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout), (StarInterface) getActivity()));
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
        //view.getBackground().setColorFilter(Color.parseColor("#00FF00"), PorterDuff.Mode.DARKEN);
//        FlightsActivity flightsActivity = (FlightsActivity)getActivity();
//        lastClickedView=flightsActivity.getSelectedView();
//        if(lastClickedView!=null) lastClickedView.setBackgroundColor(0xFFFFFFFF);
//        view.setBackgroundColor(0xFF00CCFF);
//        flightsActivity.setSelectedView(view);
        interactionListener.onFlightClicked((FlightStatus) listView.getItemAtPosition(position));
        listView.invalidateViews();
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
            boolean dualPane;

// = ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
            View detailsFrame = getActivity().findViewById(R.id.fragment_container_flight_details);
            dualPane = detailsFrame !=null && detailsFrame.getVisibility() == View.VISIBLE;


            if(dualPane){

                FlightDetailsMainFragment details = (FlightDetailsMainFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container_flight_details);
                //TODO no crearlo si es el mismo que antes
                details = new FlightDetailsMainFragment();
                FlightsActivity flightsActivity =  (FlightsActivity)getActivity();
                flightsActivity.setFlightStatus(clickedStatus);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container_flight_details,details);
                ft.commit();
            }else{
                Intent detailsIntent = new Intent(getActivity(), FlightDetailMainActivity.class);
                detailsIntent.putExtra(FlightDetailMainActivity.PARAM_STATUS, clickedStatus);
                startActivity(detailsIntent);
            }


        }
    }

}
