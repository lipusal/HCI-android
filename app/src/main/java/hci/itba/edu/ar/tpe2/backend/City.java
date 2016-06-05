package hci.itba.edu.ar.tpe2.backend;

import java.io.Serializable;

public class City implements Serializable {
    private String id, name;
    private Country country;
    private double longitude, latitude;
    private boolean has_airport;

    public City(String id, String name, String countryID, double longitude, double latitude, boolean has_airport) {
        this.id = id;
        this.name = name;
        this.country = new Country(countryID);
        this.longitude = longitude;
        this.latitude = latitude;
        this.has_airport = has_airport;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Country getCountry() {
        return country;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public boolean has_airport() {
        return has_airport;
    }

    @Override
    public String toString() {
        return name + " (" + id + ") @ (" + latitude + ", " + longitude + "), " + (has_airport ? "has " : "no ") + "airport";
    }
}
