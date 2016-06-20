package hci.itba.edu.ar.tpe2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.data.Review;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;

public class MakeReviewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private EditText reviewText;
    private Button reviewButton;
    private Flight flight;
    private int score;
    private ImageButton firstStar;
    private ImageButton secondStar;
    private ImageButton thirdStar;
    private ImageButton fourthStar;
    private ImageButton fifthStar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_review);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Creating for the first time
        if (savedInstanceState == null) {
            //wat do
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        reviewButton = (Button) findViewById(R.id.review_button);
        reviewText = (EditText) findViewById(R.id.review_text);
        Intent callerIntent = getIntent();
        flight = (Flight) callerIntent.getSerializableExtra(FlightDetailMainActivity.PARAM_FLIGHT);

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    Review review = new Review(flight, score * 2, reviewText.getText().toString());
                    API.getInstance().submitReview(review, MakeReviewActivity.this, new NetworkRequestCallback<Void>() {
                        @Override
                        public void execute(Context c, Void param) {
                            MakeReviewActivity.this.finish();
                        }
                    });

                } else {
                    //wat do
                }
            }
        });

        /**
         DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
         ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
         this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
         drawer.setDrawerListener(toggle);
         toggle.syncState();

         NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
         navigationView.setNavigationItemSelectedListener(this);
         */


        score = 0;
        firstStar = (ImageButton) findViewById(R.id.first_star_button);
        secondStar = (ImageButton) findViewById(R.id.second_star_button);
        thirdStar = (ImageButton) findViewById(R.id.third_star_button);
        fourthStar = (ImageButton) findViewById(R.id.fourth_star_button);
        fifthStar = (ImageButton) findViewById(R.id.fifth_star_button);
        firstStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score = 1;
                updateStars();
            }

        });
        secondStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score = 2;
                updateStars();
            }

        });
        thirdStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score = 3;
                updateStars();
            }

        });
        fourthStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score = 4;
                updateStars();
            }

        });
        fifthStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score = 5;
                updateStars();
            }

        });

    }


    private boolean validateFields() {
        boolean valid = true;
        String text = reviewText.getText().toString();

        if (score == 0) {
            valid = false;
            reviewText.setError("Ingrese el puntaje. Mover de lugar el error");
        }

        if (text.isEmpty()) {
            reviewText.setError("Ingrese un comentario");
            valid = false;
        }

        return valid;
    }

    private void updateStars() {
        firstStar.setImageResource(score > 0 ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
        secondStar.setImageResource(score > 1 ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
        thirdStar.setImageResource(score > 2 ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
        fourthStar.setImageResource(score > 3 ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
        fifthStar.setImageResource(score > 4 ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.make_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent i = null;
        if (id == R.id.drawer_flights) {
            i = new Intent(this, FlightsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else if (id == R.id.drawer_search) {
            i = new Intent(this, SearchActivity.class);
        } else if (id == R.id.drawer_map) {
            i = new Intent(this, DealsMapActivity.class);
        } else if (id == R.id.drawer_settings) {
            i = new Intent(this, SettingsActivity.class);
        } else if (id == R.id.drawer_help) {

        }

        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        }
        //else, unrecognized option selected, close drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
