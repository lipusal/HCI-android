package hci.itba.edu.ar.tpe2;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hci.itba.edu.ar.tpe2.backend.data.Airport;
import hci.itba.edu.ar.tpe2.backend.data.Flight;


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
    TextView originDetail;
    TextView arrivalDetail;
    TextView extraDetail;
    Flight flight;


    public FlightDetailsFragment() {
        // Required empty public constructor
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view=inflater.inflate(R.layout.fragment_flight_details, container, false);

        final FlightDetailMainActivity activity = (FlightDetailMainActivity) getActivity();
        final Flight flight = activity.getFlight();

        Airport departureAirport = flight.getDepartureAirport();
        Airport arrivalAirport = flight.getArrivalAirport();

        firstPartDetail = (TextView)view.findViewById(R.id.firstPartDetail);
        originDetail = (TextView)view.findViewById(R.id.originDetail);
        arrivalDetail = (TextView)view.findViewById(R.id.arrivalDetail);
        extraDetail = (TextView)view.findViewById(R.id.extraDetail);

        FragmentActivity context = getActivity();

        firstPartDetail.setText(flight.getAirline().getName() + "(" + flight.getAirline().getID() + ")#" + flight.getNumber() + "" +
                "\n" + departureAirport.getID() + "->" + flight.getArrivalAirport().getID() + "\n" /*+
                "Estado: "+flight.getStatus().getStatus()*/);   //FIXME los vuelos no vienen con estado, habría que hacer una query por vuelo (no lo vamos a hacer). Los únicos vuelos que tienen estado guardado son los que sigue el usuario
        originDetail.setText(context.getString(R.string.origin)+"\n" + //Usar spannableString para el size?
                ""+ departureAirport.getDescription()+", "+departureAirport.getCity().getName()+", " +
                departureAirport.getCity().getCountry().getName()+"" +
                flight.getPrettyDepartureDate()+"\n" +
                departureAirport.getID() + R.string.terminal + R.string.gate + "\n"+
                flight.getPrettyDepartureDate() + "   "/*+flight.getStatus().getDepartureTerminal() + "   "+  flight.getStatus().getDepartureGate()*/);

        arrivalDetail.setText(R.string.arrival+"\n" + //Usar spannableString para el size?
                ""+ arrivalAirport.getDescription()+", "+arrivalAirport.getCity().getName()+", " +
                arrivalAirport.getCity().getCountry().getName()+"" +
                flight.getPrettyArrivalDate()+"\n" +
                arrivalAirport.getID() + R.string.terminal + R.string.gate +
                flight.getPrettyArrivalDate() + "   "/*+flight.getStatus().getArrivalTerminal() + "   "+  flight.getStatus().getArrivalGate()*/);

        extraDetail.setText(R.string.extra_details+"\n" +
                R.string.direct_flight+"\n" + //Hardcodeado?
                R.string.duration+ flight.getDurationStr() + "\n" +
                R.string.equipage+ "Donde? \n" +
                R.string.price + flight.getTotal() + "\n" +
                R.string.score + "puntaje?Aca?  ");

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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
