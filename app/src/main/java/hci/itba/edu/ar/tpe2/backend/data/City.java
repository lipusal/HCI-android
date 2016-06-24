package hci.itba.edu.ar.tpe2.backend.data;

import android.text.Html;

import com.google.gson.JsonObject;

import java.io.Serializable;

public class City extends Place implements Serializable {
    private String id, name;
    private Country country;
    private double longitude, latitude;
    private boolean has_airport;
    private String flickrUrl;

    private City(String id, String name, String countryID, double longitude, double latitude, boolean has_airport) {
        this.id = id;
        this.name = name;
        this.country = PersistentData.getContextLessInstance().getCountries().get(countryID);
        this.longitude = longitude;
        this.latitude = latitude;
        this.has_airport = has_airport;
    }

    public static City fromJson(JsonObject json) {
        return new City(
                json.get("id").getAsString(),
                Html.fromHtml(json.get("name").getAsString()).toString(),
                json.getAsJsonObject("country").get("id").getAsString(),
                json.get("longitude").getAsDouble(),
                json.get("latitude").getAsDouble(),
                json.get("has_airport").getAsBoolean()
        );
    }

    @Override
    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFlickrUrl() {
        return flickrUrl;
    }

    public void setFlickrUrl(String url) {
        flickrUrl = url;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country completeCountry) {
        this.country = completeCountry;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
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
