package hci.itba.edu.ar.tpe2;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

    private boolean firstTime;

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
        firstTime = true;

//        if (getArguments() != null) {
        //          mParam1 = getArguments().getString(ARG_PARAM1);
        //        mParam2 = getArguments().getString(ARG_PARAM2);
        //  }


    }

    @Override
    public void onResume() {
        super.onResume();
        setView();

    }

    private void setView() {
        if (firstTime) {
            title.setText("Searching...");
            firstTime = false;
        } else {
            title.setText("Updating...");
        }
        final FlightDetailMainActivity activity = (FlightDetailMainActivity) getActivity();
        final Flight flight = activity.getFlight();

        API.getInstance().getAllReviews(flight, activity, new NetworkRequestCallback<Review[]>() {
            @Override
            public void execute(Context c, Review[] result) {
                reviews = new ArrayList<Review>(Arrays.asList(result));
                if (reviewsAdapter == null) {
                    reviewsAdapter = new ReviewAdapter(activity, reviews);
                    reviewsList.setAdapter(reviewsAdapter);
                } else {
                    reviewsAdapter.clear();
                    reviewsAdapter.addAll(reviews);
                    reviewsAdapter.notifyDataSetChanged();
                }
                title.setText("");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_flight_reviews, container, false);
        if (savedInstanceState == null) {
            title = (TextView) view.findViewById(R.id.reviews_results_title);
            reviewsList = (ListView) view.findViewById(R.id.reviews_list);
        }
        //setView();

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

    private class ReviewAdapter extends ArrayAdapter<Review> {

        ReviewAdapter(Context context, List<Review> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View destination, ViewGroup parent) {
            Review review = getItem(position);

            if (destination == null) {
                destination = LayoutInflater.from(getContext()).inflate(R.layout.review_list, parent, false);
            }
            //Fill in the list item with data
            TextView text = (TextView) destination.findViewById(R.id.textReview);//,

            text.setText(review.getComment() + " \n" + review.getOverall() + "/5");
            /*


            */
            int overall = review.getOverall();

            ImageView firstStar = (ImageView) destination.findViewById(R.id.firstStar);
            ImageView secondStar = (ImageView) destination.findViewById(R.id.secondStar);
            ImageView thirdStar = (ImageView) destination.findViewById(R.id.thirdStar);
            ImageView fourthStar = (ImageView) destination.findViewById(R.id.fourthStar);
            ImageView fifthStar = (ImageView) destination.findViewById(R.id.fifthStar);

            firstStar.setImageResource(overall > 0 ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
            secondStar.setImageResource(overall > 1 ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
            thirdStar.setImageResource(overall > 2 ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
            fourthStar.setImageResource(overall > 3 ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
            fifthStar.setImageResource(overall > 4 ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);

            return destination;
        }
    }
}
