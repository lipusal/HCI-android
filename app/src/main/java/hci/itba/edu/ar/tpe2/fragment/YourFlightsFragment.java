package hci.itba.edu.ar.tpe2.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.service.UpdatePriorityReceiver;
import hci.itba.edu.ar.tpe2.backend.service.UpdateService;


/**
 * Fragment used to hold the logic of creating different views for phones and tablets. The fragment
 * will, one way or another, have the responsibility of showing a list of followed flights and the
 * ability to see their details.
 */
public class YourFlightsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private UpdatePriorityReceiver updatesReceiver;
    private IntentFilter broadcastPriorityFilter;
    private PersistentData persistentData;

    //View elements
    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private Menu menu;
    private FlightStatusListFragment flightsFragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OnFragmentInteractionListener mListener;

    //Logic
    private boolean reviewVisiblle;
    private boolean isDualPaneEnabled;


    public YourFlightsFragment() {
        // Required empty public constructor
    }

    public static YourFlightsFragment newInstance(CoordinatorLayout coordinatorLayout) {
        YourFlightsFragment result = new YourFlightsFragment();
        result.coordinatorLayout = coordinatorLayout;
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_your_flights, container, false);

        //Broadcast receiver with high priority
        broadcastPriorityFilter = new IntentFilter(UpdateService.ACTION_UPDATE_COMPLETE);
        broadcastPriorityFilter.setPriority(1);
        updatesReceiver = new UpdatePriorityReceiver(coordinatorLayout) {
            @Override
            public void onNoFlightsChanged(boolean manuallyTriggered) {
                //Manual update completed
                if (manuallyTriggered) {
                    swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(destinationView, R.string.no_changes, Snackbar.LENGTH_LONG).show();
                }
                //Automatic update completed, do default behavior
                else {
                    super.onNoFlightsChanged(manuallyTriggered);
                }
            }

            @Override
            public void onSingleFlightChanged(FlightStatus newStatus, boolean manuallyTriggered) {
                super.onSingleFlightChanged(newStatus, manuallyTriggered);
                swipeRefreshLayout.setRefreshing(false);
                refreshFlights();
            }

            @Override
            public void onMultipleFlightsChanged(Collection<FlightStatus> newStatuses, boolean manuallyTriggered) {
                swipeRefreshLayout.setRefreshing(false);
                refreshFlights();
                //Same snackbar as super, but no action (user is already in Flights activity)
                Snackbar.make(destinationView, String.format(getString(R.string.x_flights_updated), newStatuses.size()), Snackbar.LENGTH_LONG).show();
            }
        };

        //Handle refreshes
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(view.getContext(), R.color.colorAccent),
                ContextCompat.getColor(view.getContext(), R.color.colorPrimary));

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        refreshFlights();
        //(Re-)register updates receiver
        getActivity().registerReceiver(updatesReceiver, broadcastPriorityFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(updatesReceiver);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        View detailsFrame = getActivity().findViewById(R.id.fragment_container_flight_details);
        isDualPaneEnabled = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        persistentData = new PersistentData(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        void onFragmentInteraction(Uri uri);
    }

    /**
     * (Re-)fills the fragment container with the latest info in the followed flights. If there are
     * no watched flights, places a text fragment to show this. Otherwise, places a list fragment
     * listing the status of all watched flights with their latest available status.
     */
    private void refreshFlights() {
        //Add/refresh the flights fragment, enable/disable swipe to refresh
        Map<Integer, FlightStatus> watchedFlights = persistentData.getWatchedStatuses();
        FragmentManager fm = getChildFragmentManager();
        if (watchedFlights == null || watchedFlights.isEmpty()) {
            //No watched flights, put text fragment in the fragment container
            swipeRefreshLayout.setEnabled(false);
            TextFragment textFragment = TextFragment.newInstance(getString(R.string.not_following_flights));
            if (flightsFragment == null) {    //Creating for the first time
                fm.beginTransaction().add(R.id.fragment_container, textFragment).commit();
            } else {
                fm.beginTransaction().replace(R.id.fragment_container, textFragment).commit();
                flightsFragment = null;
            }
        } else {
            //Watched flights, put text flights list fragment in the fragment container
            List<FlightStatus> list = new ArrayList<>(watchedFlights.size());
            list.addAll(watchedFlights.values());

            flightsFragment = FlightStatusListFragment.newInstance(list);
            if (flightsFragment == null) {    //Creating for the first time
                fm.beginTransaction().add(R.id.fragment_container, flightsFragment).commit();
            } else {
                fm.beginTransaction().replace(R.id.fragment_container, flightsFragment).commit();
            }

            //Override scroll behavior for swipe-to-refresh to work properly: http://stackoverflow.com/a/35779571/2333689
            fm.executePendingTransactions();   //Otherwise the view in the next line might be null
            final ListView flightsFragmentListView = flightsFragment.getListView();
            flightsFragmentListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (flightsFragmentListView.getChildAt(0) != null) {
                        swipeRefreshLayout.setEnabled(flightsFragmentListView.getFirstVisiblePosition() == 0 && flightsFragmentListView.getChildAt(0).getTop() == 0);
                    }
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        Intent intent = new Intent(getActivity(), UpdateService.class);
        intent.setAction(UpdateService.ACTION_CHECK_FOR_UPDATES);
        intent.putExtra(UpdateService.EXTRA_MANUAL_UPDATE, true);
        getActivity().startService(intent);
        swipeRefreshLayout.setRefreshing(true);
    }
}
