package hci.itba.edu.ar.tpe2.fragment;

import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;

import hci.itba.edu.ar.tpe2.FlightDetailMainActivity;
import hci.itba.edu.ar.tpe2.FlightsActivity;
import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.Airport;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.service.UpdatePriorityReceiver;
import hci.itba.edu.ar.tpe2.backend.service.UpdateService;

/**
 * Fragment used to show details of a flight, given its Status object.
 */
public class FlightDetailsFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public static final String PARAM_FLIGHT = "FLIGHT";

    TextView title,
            subtitle,
            departureGate,
            departureTerminal,
            scheduledDepartureTime,
            actualDepartureTime,
            departureDelay,
            departureAirportText,
            arrivalGate,
            arrivalTerminal,
            scheduledArrivalTime,
            actualArrivalTime,
            arrivalDelay,
            arrivalAirportText,
            arrivalBaggageClaim;
    //"Hideable" elements
    View titleDivider, departure_arrivalDivider;
    TextView departureAirportLabel,
            scheduledDepartureTimeLabel,
            actualDepartureTimeLabel,
            departureDelayLabel,
            departureTerminalLabel,
            departureGateLabel,
            arrivalAirportLabel,
            scheduledArrivalTimeLabel,
            actualArrivalTimeLabel,
            arrivalDelayLabel,
            arrivalTerminalLabel,
            arrivalGateLabel,
            arrivalBaggageClaimLabel;


    private FlightStatus status;
    private UpdatePriorityReceiver updatesReceiver;
    private IntentFilter priorityFilter;

    public FlightDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
        //(Re-)register refresh broadcast receiver
        getActivity().registerReceiver(updatesReceiver, priorityFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(updatesReceiver);
    }

    public void updateView() {
//        switch (status.getStatus()) {
//            case "L": extraDetail.setCompoundDrawablesWithIntrinsicBounds(getActivity().getResources().getDrawable(R.drawable.ic_flight_arriving, getActivity().getTheme()), null, getActivity().getResources().getDrawable(R.drawable.ic_flight_arriving, getActivity().getTheme()), null);
//            case "C": extraDetail.setCompoundDrawablesWithIntrinsicBounds(getActivity().getResources().getDrawable(R.drawable.ic_flight_canceled, getActivity().getTheme()), null, getActivity().getResources().getDrawable(R.drawable.ic_flight_canceled, getActivity().getTheme()), null);;
//            case "D": extraDetail.setCompoundDrawablesWithIntrinsicBounds(getActivity().getResources().getDrawable(R.drawable.ic_flight_diverging, getActivity().getTheme()), null, getActivity().getResources().getDrawable(R.drawable.ic_flight_diverging, getActivity().getTheme()), null);;
//            case "S": extraDetail.setCompoundDrawablesWithIntrinsicBounds(getActivity().getResources().getDrawable(R.drawable.ic_flight_schelduled, getActivity().getTheme()), null, getActivity().getResources().getDrawable(R.drawable.ic_flight_schelduled, getActivity().getTheme()), null);;
//            case "F": extraDetail.setCompoundDrawablesWithIntrinsicBounds(getActivity().getResources().getDrawable(R.drawable.ic_flight_flying, getActivity().getTheme()), null, getActivity().getResources().getDrawable(R.drawable.ic_flight_flying, getActivity().getTheme()), null);
//            default:;
//        }
        Airport departureAirport = status.getOriginAirport();
        Airport arrivalAirport = status.getDestinationAirport();
        if (status == null) {
            title.setText("Not Found"); //TODO show something else and use String resource
            //Hide all other elements
            titleDivider.setVisibility(View.GONE);
            departure_arrivalDivider.setVisibility(View.GONE);
            departureAirportLabel.setVisibility(View.GONE);
            scheduledDepartureTimeLabel.setVisibility(View.GONE);
            actualDepartureTimeLabel.setVisibility(View.GONE);
            departureDelayLabel.setVisibility(View.GONE);
            departureTerminalLabel.setVisibility(View.GONE);
            departureGateLabel.setVisibility(View.GONE);
            arrivalAirportLabel.setVisibility(View.GONE);
            scheduledArrivalTimeLabel.setVisibility(View.GONE);
            actualArrivalTimeLabel.setVisibility(View.GONE);
            arrivalDelayLabel.setVisibility(View.GONE);
            arrivalTerminalLabel.setVisibility(View.GONE);
            arrivalGateLabel.setVisibility(View.GONE);
            arrivalBaggageClaimLabel.setVisibility(View.GONE);
        } else {
            String firstPartDetailStr = status.getAirline().getName() + " (" + status.getAirline().getID() + ") #" + status.getFlight().getNumber();
            String firstPartDetailStr2 = "Desde: " + departureAirport.getID() + " con destino a " + arrivalAirport.getID();
            //Title
            title.setText(firstPartDetailStr);
            subtitle.setText(status.getStringResID());
            subtitle.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getActivity().getDrawable(status.getIconID()), null);
            //Departure
            departureAirportText.setText(departureAirport.toString());
            scheduledDepartureTime.setText(status.getPrettyScheduledDepartureTime());   //TODO use proper timezone, this always uses the phone's (which may be different than the airport's)
            actualDepartureTime.setText(status.getPrettyActualDepartureTime());      //TODO use proper timezone
            departureDelay.setText(status.getPrettyDepartureDelay());
            scheduledDepartureTime.setText(status.getPrettyScheduledDepartureTime());
            departureTerminal.setText(status.getDepartureTerminal());
            departureGate.setText(status.getDepartureGate());
            //Arrival
            arrivalAirportText.setText(arrivalAirport.toString());
            scheduledArrivalTime.setText(status.getPrettyScheduledArrivalTime());        //TODO use proper timezone, this always uses the phone's (which may be different than the airport's)
            actualArrivalTime.setText(status.getPrettyActualArrivalTime());      //TODO use proper timezone
            arrivalDelay.setText(status.getPrettyArrivalDelay());
            arrivalTerminal.setText(status.getArrivalTerminal());
            arrivalGate.setText(status.getArrivalGate());
            arrivalBaggageClaim.setText(status.getBaggageClaim());
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_flight_details, container, false);

        //Get the status of the parent activity -- very fragile, casts are ugly
        try {
            FlightDetailMainActivity activity = (FlightDetailMainActivity) getActivity();
            status = activity.getFlightStatus();
        } catch (ClassCastException e) {
            FlightsActivity activity = (FlightsActivity) getActivity();
            status = activity.getFlightStatus();
        }

        //Set up broadcast receiver with priority (needs a View to work properly)
        priorityFilter = UpdatePriorityReceiver.getPriorityFilter();
        updatesReceiver = new UpdatePriorityReceiver(view) {
            @Override
            public void onSingleFlightChanged(FlightStatus newStatus, boolean manualUpdate) {
                //If this flight was updated, refresh the view and show a special snackbar (no action, user is already in this flight's details activity), otherwise do default behavior
                if (newStatus.getFlight().equals(status.getFlight())) {
                    status = newStatus;
                    Snackbar.make(view, R.string.flight_updated, Snackbar.LENGTH_LONG).show();
                    updateView();
                } else {
                    super.onSingleFlightChanged(newStatus, manualUpdate);
                }
            }

            @Override
            public void onMultipleFlightsChanged(Collection<FlightStatus> newStatuses, boolean manualUpdate) {
                //If this flight was updated, refresh the view and show a special snackbar (no action, user is already in this flight's details activity), otherwise do default behavior
                boolean found = false;
                for (FlightStatus updatedStatus : newStatuses) {
                    if (updatedStatus.getFlight().equals(status.getFlight())) {
                        found = true;
                        status = updatedStatus;
                        Snackbar.make(view, R.string.flight_updated, Snackbar.LENGTH_LONG).show();
                        updateView();
                        break;
                    }
                }
                if (!found) {
                    super.onMultipleFlightsChanged(newStatuses, manualUpdate);
                }
            }
        };

        //Bind all the view elements
        title = (TextView) view.findViewById(R.id.title);
        subtitle = (TextView) view.findViewById(R.id.subtitle);
        departureAirportText = (TextView) view.findViewById(R.id.departureAirport);
        departureGate = (TextView) view.findViewById(R.id.departureGateText);
        departureTerminal = (TextView) view.findViewById(R.id.departureTerminalText);
        scheduledDepartureTime = (TextView) view.findViewById(R.id.scheduledDepartureTimeText);
        actualDepartureTime = (TextView) view.findViewById(R.id.actualDepartureTimeText);
        departureDelay = (TextView) view.findViewById(R.id.departureDelayText);
        arrivalAirportText = (TextView) view.findViewById(R.id.arrivalAirport);
        arrivalGate = (TextView) view.findViewById(R.id.arrivalGateText);
        arrivalTerminal = (TextView) view.findViewById(R.id.arrivalTerminalText);
        scheduledArrivalTime = (TextView) view.findViewById(R.id.scheduledArrivalTimeText);
        actualArrivalTime = (TextView) view.findViewById(R.id.actualArrivalTimeText);
        arrivalDelay = (TextView) view.findViewById(R.id.arrivalDelayText);
        arrivalBaggageClaim = (TextView) view.findViewById(R.id.arrivalBaggageClaimText);
        //"Hideable" elements
        titleDivider = view.findViewById(R.id.titleDivider);
        departure_arrivalDivider = view.findViewById(R.id.departureArrivalDivider);
        departureAirportLabel = (TextView) view.findViewById(R.id.departureAirport);
        departureGateLabel = (TextView) view.findViewById(R.id.departureGateLabel);
        departureTerminalLabel = (TextView) view.findViewById(R.id.departureTerminalLabel);
        scheduledDepartureTimeLabel = (TextView) view.findViewById(R.id.scheduledDepartureTimeLabel);
        actualDepartureTimeLabel = (TextView) view.findViewById(R.id.actualDepartureTimeLabel);
        departureDelayLabel = (TextView) view.findViewById(R.id.departureDelayLabel);
        arrivalAirportLabel = (TextView) view.findViewById(R.id.arrivalAirport);
        scheduledArrivalTimeLabel = (TextView) view.findViewById(R.id.scheduledArrivalTimeLabel);
        actualArrivalTimeLabel = (TextView) view.findViewById(R.id.actualArrivalTimeLabel);
        arrivalDelayLabel = (TextView) view.findViewById(R.id.arrivalDelayLabel);
        arrivalGateLabel = (TextView) view.findViewById(R.id.arrivalGateLabel);
        arrivalTerminalLabel = (TextView) view.findViewById(R.id.arrivalTerminalLabel);
        arrivalBaggageClaimLabel = (TextView) view.findViewById(R.id.arrivalBaggageClaimLabel);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
