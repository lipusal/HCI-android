package hci.itba.edu.ar.tpe2;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.Review;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FlightReviewsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FlightReviewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FlightReviewsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private List<Review> reviews;
    //View elements
    private ListView reviewsList;
    private ReviewAdapter reviewsAdapter;
    private TextView title;

    private OnFragmentInteractionListener mListener;

    public FlightReviewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment FlightReviewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FlightReviewsFragment newInstance() {
        FlightReviewsFragment fragment = new FlightReviewsFragment();
      //  Bundle args = new Bundle();

       // fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
  //          mParam1 = getArguments().getString(ARG_PARAM1);
    //        mParam2 = getArguments().getString(ARG_PARAM2);
      //  }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_flight_reviews, container, false);
        if (savedInstanceState == null) {
            title = (TextView) view.findViewById(R.id.reviews_results_title);
            reviewsList = (ListView) view.findViewById(R.id.reviews_list);
        }
        title.setText("Searching...");
        final FlightDetailMainActivity activity = (FlightDetailMainActivity) getActivity();
        final Flight flight = activity.getFlight();

        API.getInstance().getAllReviews(flight, activity, new NetworkRequestCallback<Review[]>() {
            @Override
            public void execute(Context c,Review[] result) {
                reviews = new ArrayList<Review>(Arrays.asList(result));
                if (reviewsAdapter == null) {
                    reviewsAdapter = new ReviewAdapter(activity, reviews);
                    reviewsList.setAdapter(reviewsAdapter);
                } else {
                    reviewsAdapter.clear();
                    reviewsAdapter.addAll(reviews);
                    reviewsAdapter.notifyDataSetChanged();
                }
                title.setText("NADA, BORRAR?!?!!");
            }
        });

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

    private class ReviewAdapter extends ArrayAdapter<Review> {

        ReviewAdapter(Context context, List<Review> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View destination, ViewGroup parent) {
            Review review = getItem(position);

            if (destination == null) {  //Item hasn't been created, inflate it from Android's default layout
                destination = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            //Fill in the list item with data
            TextView title = (TextView) destination.findViewById(android.R.id.text1);//,
//                    subtitle = (TextView) destination.findViewById(android.R.id.text2);
            title.setText(review.getComment() + " \n" + review.getOverall() +"/5");
//            subtitle.setText(flight.getPrettyDepartureDate());
            return destination;
        }
    }
}
