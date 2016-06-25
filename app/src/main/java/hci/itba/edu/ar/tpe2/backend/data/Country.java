package hci.itba.edu.ar.tpe2.backend.data;

import android.text.Html;

import com.google.gson.JsonObject;

import java.io.Serializable;

public class Country implements Serializable {
    private String id, name;
    private double longitude, latitude;
    private boolean hasCoords;

    private Country(String id, String name, double longitude, double latitude) {
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public static Country fromJson(JsonObject json) {
        return new Country(
                json.get("id").getAsString(),
                Html.fromHtml(json.get("name").getAsString()).toString(),
                json.get("longitude").getAsDouble(),
                json.get("latitude").getAsDouble()
        );
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String toString() {
        if(name == null) {
            return "Unnamed country (" + id + ")";
        }
        else {
            return name + " (" + id + ") @ (" + latitude + ", " + longitude + ")";
        }
    }
}
