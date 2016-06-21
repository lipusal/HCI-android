package hci.itba.edu.ar.tpe2;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.data.Review;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;

/**
 * Fragment used as part of a SwipeLayout to show reviews of a specified flight.
 */
public class FlightReviewsFragment extends Fragment {
    private boolean firstTime;
    private List<Review> reviews;

    //View elements
    private ListView reviewsList;
    private ReviewAdapter reviewsAdapter;
    private TextView title;

    public FlightReviewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstTime = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    private void updateView() {
        if (firstTime) {
            title.setText("Searching...");
            firstTime = false;
        } else {
            title.setText("Updating...");
        }
        title.setVisibility(View.VISIBLE);
        final FlightDetailMainActivity activity = (FlightDetailMainActivity) getActivity();
       
        API.getInstance().getAllReviews(activity.getFlightStatus().getFlight(), activity, new NetworkRequestCallback<Review[]>() {
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
                title.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_flight_reviews, container, false);

        title = (TextView) view.findViewById(R.id.reviews_results_title);
        reviewsList = (ListView) view.findViewById(R.id.reviews_list);
        //updateView();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
