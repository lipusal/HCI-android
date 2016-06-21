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
import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.service.NotificationService;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FlightDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FlightDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FlightDetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public static final String PARAM_FLIGHT = "FLIGHT";

    TextView firstPartDetail;
    TextView originDetail1;
    TextView originDetail2;
    TextView originDetail3;
    TextView originTitle;
    TextView arrivalDetail1;
    TextView arrivalDetail2;
    TextView arrivalDetail3;
    TextView arrivalTitle;
    TextView extraDetail;
    FlightStatus flightStatus;

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



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FlightDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FlightDetailsFragment newInstance(String param1, String param2) {
        FlightDetailsFragment fragment = new FlightDetailsFragment();
        Bundle args = new Bundle();
        //   args.putString(ARG_PARAM1, param1);
        //   args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //        mParam1 = getArguments().getString(ARG_PARAM1);
            //        mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void updateView(){
        FragmentActivity context = getActivity();
        Airport departureAirport = flightStatus.getOriginAirport();
        Airport arrivalAirport = flightStatus.getDestinationAirport();
        firstPartDetail.setText("ALUHAKBAR ALEXIS!!");

//        Flight flight = flightStatus.getFlight();
//
//        firstPartDetail.setText(flight.getAirline().getName() + "(" + flight.getAirline().getID() + ")#" + flight.getNumber() + "" +
//                "\n" + departureAirport.getID() + "->" + flight.getArrivalAirport().getID() + "\n" /*+
//                "Estado: "+flight.getStatus().getStatus()*/);   //FIXME los vuelos no vienen con estado, habría que hacer una query por vuelo (no lo vamos a hacer). Los únicos vuelos que tienen estado guardado son los que sigue el usuario
//        originTitle.setText("Origen");
//        originTitle.setCompoundDrawablesWithIntrinsicBounds(getActivity().getResources().getDrawable(R.drawable.ic_flight_takeoff_black, getActivity().getTheme()), null, null, null);
//        originDetail1.setText(departureAirport.getDescription() + ", " + departureAirport.getCity().getName() + ", " +
//                departureAirport.getCity().getCountry().getName() + "" +
//                flight.getPrettyDepartureDate() + "\n" +
//                departureAirport.getID() + context.getString(R.string.terminal) + context.getString(R.string.gate) + "\n" +
//                flight.getPrettyDepartureDate() + "   "/*+flight.getStatus().getDepartureTerminal() + "   "+  flight.getStatus().getDepartureGate()*/);
//        originDetail2.setText("Ayy lmao");
//        originDetail3.setText("Dank");
//        arrivalTitle.setText("Destino");
//        arrivalTitle.setCompoundDrawablesWithIntrinsicBounds(getActivity().getResources().getDrawable(R.drawable.ic_flight_land_black, getActivity().getTheme()), null, null, null);
//        arrivalDetail1.setText(arrivalAirport.getDescription() + ", " + arrivalAirport.getCity().getName() + ", " +
//                arrivalAirport.getCity().getCountry().getName() + "" +
//                flight.getPrettyArrivalDate() + "\n" +
//                arrivalAirport.getID() + context.getString(R.string.terminal) + context.getString(R.string.gate) +
//                flight.getPrettyArrivalDate() + "   "/*+flight.getStatus().getArrivalTerminal() + "   "+  flight.getStatus().getArrivalGate()*/);
//        arrivalDetail2.setText("allo");
//        arrivalDetail3.setText("bpnjouir");
//        extraDetail.setText(context.getString(R.string.extra_details) + "\n" +
//                context.getString(R.string.direct_flight) + "\n" + //Hardcodeado?
//                context.getString(R.string.duration) + flight.getDurationStr() + "\n" +
//                context.getString(R.string.equipage) + "Donde? \n" +
//                context.getString(R.string.price) + flight.getTotal() + "\n" +
//                context.getString(R.string.score) + "puntaje?Aca?  ");


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_flight_details, container, false);

        final FlightDetailMainActivity activity = (FlightDetailMainActivity) getActivity();
        flightStatus = activity.getFlightStatus();



        firstPartDetail = (TextView) view.findViewById(R.id.firstPartDetail);
        originDetail1 = (TextView) view.findViewById(R.id.originDetail1);
        originDetail2 = (TextView) view.findViewById(R.id.originDetail2);
        originDetail3 = (TextView) view.findViewById(R.id.originDetail3);
        originTitle = (TextView) view.findViewById(R.id.originTitle);
        arrivalDetail1 = (TextView) view.findViewById(R.id.arrivalDetail1);
        arrivalDetail2 = (TextView) view.findViewById(R.id.arrivalDetail2);
        arrivalDetail3 = (TextView) view.findViewById(R.id.arrivalDetail3);
        arrivalTitle = (TextView) view.findViewById(R.id.arrivalTitle);
        extraDetail = (TextView) view.findViewById(R.id.extraDetail);


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
