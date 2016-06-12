package hci.itba.edu.ar.tpe2;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import hci.itba.edu.ar.tpe2.backend.data.Airport;
import hci.itba.edu.ar.tpe2.backend.data.Flight;

public class FlightDetailsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String PARAM_FLIGHT = "FLIGHT";

        TextView firstPartDetail;
        TextView originDetail;
        TextView arrivalDetail;
        TextView extraDetail;
        Flight flight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get info from the specified flight
        Intent callerIntent = getIntent();
        if (!callerIntent.hasExtra(PARAM_FLIGHT)) {
            throw new IllegalStateException("Flight details activity started without " + PARAM_FLIGHT + " parameter in Intent");
        }
        flight = (Flight) callerIntent.getSerializableExtra(PARAM_FLIGHT);
        Airport departureAirport = flight.getDepartureAirport();
        Airport arrivalAirport = flight.getArrivalAirport();

        firstPartDetail = (TextView)findViewById(R.id.firstPartDetail);
        originDetail = (TextView)findViewById(R.id.originDetail);
        arrivalDetail = (TextView)findViewById(R.id.arrivalDetail);
        extraDetail = (TextView)findViewById(R.id.extraDetail);


        firstPartDetail.setText(flight.getAirline().getName() + "(" + flight.getAirline().getID() + ")#" + flight.getID() + "" +
                "\n" + departureAirport.getID() + "->" + flight.getArrivalAirport().getID() + "\n" /*+
                "Estado: "+flight.getStatus().getStatus()*/);   //FIXME los vuelos no vienen con estado, habría que hacer una query por vuelo (no lo vamos a hacer). Los únicos vuelos que tienen estado guardado son los que sigue el usuario
        originDetail.setText("Origen\n" + //Usar spannableString para el size?
                ""+ departureAirport.getDescription()+", "+departureAirport.getCity().getName()+", " +
                departureAirport.getCity().getCountry().getName()+"" +
                flight.getPrettyDepartureDate()+"\n" +
                departureAirport.getID() + "  Terminal  " + " Puerta\n" +
                flight.getPrettyDepartureDate() + "   "/*+flight.getStatus().getDepartureTerminal() + "   "+  flight.getStatus().getDepartureGate()*/);

        arrivalDetail.setText("Arrival\n" + //Usar spannableString para el size?
                ""+ arrivalAirport.getDescription()+", "+arrivalAirport.getCity().getName()+", " +
                arrivalAirport.getCity().getCountry().getName()+"" +
                flight.getPrettyArrivalDate()+"\n" +
                arrivalAirport.getID() + "  Terminal  " + " Puerta\n" +
                flight.getPrettyArrivalDate() + "   "/*+flight.getStatus().getArrivalTerminal() + "   "+  flight.getStatus().getArrivalGate()*/);

        extraDetail.setText("Otros Detalles \n" +
                "Vuelo directo \n" + //Hardcodeado?
                "Duracion: "+ flight.getDurationStr() + "\n" +
                "Recolection de equipage "+ "Donde? \n" +
                "Precio: " + flight.getTotal() + "\n" +
                "Puntaje: " + "puntaje?");


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        getMenuInflater().inflate(R.menu.flight_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
