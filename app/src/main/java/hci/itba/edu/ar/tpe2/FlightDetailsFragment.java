package hci.itba.edu.ar.tpe2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hci.itba.edu.ar.tpe2.backend.data.Airport;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.service.NotificationService;

/**
 * Fragment used to show details of a flight, given its Status object.
 */
public class FlightDetailsFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public static final String PARAM_FLIGHT = "FLIGHT";

    TextView title;
    TextView flightSubtitle;
    TextView departureGate;
    TextView departureTerminal;
    TextView departureTime;
    TextView departureAirportText;
    TextView arrivalGate;
    TextView arrivalTerminal;
    TextView arrivalTime;
    TextView arrivalAirportText;
    TextView arrivalBaggageClaim;
    //    TextView extraDetail;
    FlightStatus status;

    private BroadcastReceiver refreshCompleteBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateView();
        }
    };

    public FlightDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        //(Re-)register refresh broadcast receiver
        updateView();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshCompleteBroadcastReceiver, new IntentFilter(NotificationService.ACTION_UPDATES_COMPLETE));

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(refreshCompleteBroadcastReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void updateView(){
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
        if(arrivalAirport == null && departureAirport == null) {
            title.setText("Not Found");
        }
        else {
            String firstPartDetailStr = status.getAirline().getName() + " (" + status.getAirline().getID() + ") #" + status.getFlight().getNumber();
            String firstPartDetailStr2 = "Desde: " + departureAirport.getID() + " con destino a " + arrivalAirport.getID();
            //Title
            title.setText(firstPartDetailStr);
            flightSubtitle.setText(status.getStringResID());
            flightSubtitle.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getActivity().getDrawable(status.getIconID()), null);
            //Departure
            departureAirportText.setText(departureAirport.toString());
            departureTime.setText(status.getPrettyScheduledDepartureTime());
            departureTerminal.setText(status.getDepartureTerminal() == null ? "—" : status.getDepartureTerminal());
            departureGate.setText(status.getDepartureGate() == null ? "—" : status.getDepartureGate());
            //Arrival
            arrivalAirportText.setText(arrivalAirport.toString());
            arrivalTime.setText(status.getPrettyScheduledArrivalTime());
            arrivalTerminal.setText(status.getArrivalTerminal() == null ? "—" : status.getArrivalTerminal());
            arrivalGate.setText(status.getArrivalGate() == null ? "—" : status.getArrivalGate());
            arrivalBaggageClaim.setText(status.getBaggageClaim() == null ? "—" : status.getBaggageClaim());

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_flight_details, container, false);

        final FlightDetailMainActivity activity = (FlightDetailMainActivity) getActivity();
        status = activity.getFlightStatus();

        title = (TextView) view.findViewById(R.id.title);
        flightSubtitle = (TextView) view.findViewById(R.id.subtitle);
        departureGate = (TextView) view.findViewById(R.id.originGateText);
        departureTerminal = (TextView) view.findViewById(R.id.departureTerminalText);
        departureTime = (TextView) view.findViewById(R.id.departureTimeText);
        departureAirportText = (TextView) view.findViewById(R.id.departureAirport);
        arrivalGate = (TextView) view.findViewById(R.id.arrivalGateText);
        arrivalTerminal = (TextView) view.findViewById(R.id.arrivalTerminalText);
        arrivalTime = (TextView) view.findViewById(R.id.arrivalDepartureTimeText);
        arrivalAirportText = (TextView) view.findViewById(R.id.arrivalAirport);
        arrivalBaggageClaim = (TextView) view.findViewById(R.id.arrivalBaggageClaimText);
//        extraDetail = (TextView) view.findViewById(R.id.extraDetail);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
