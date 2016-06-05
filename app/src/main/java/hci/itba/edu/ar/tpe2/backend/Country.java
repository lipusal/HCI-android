package hci.itba.edu.ar.tpe2.backend;

import java.io.Serializable;

public class Country implements Serializable {
    private String id, name;
    private double longitude, latitude;
    private boolean hasCoords;

    public Country(String id, String name, double longitude, double latitude) {
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.hasCoords = true;
    }

    public Country(String id) {
        this(id, null, 0, 0);
        hasCoords = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns whether this object was constructed with coordinates or not.
     *
     * @return <code>true</code> if this object was instatiated with coordinates, <code>false</code>
     * otherwise.
     */
    public boolean hasCoords() {
        return hasCoords;
    }

    /**
     * @return This country's longitude. If 0, may indicate it was constructed without coordinates.
     * @see #hasCoords()
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @return This country's latitude. If 0, may indicate it was constructed without coordinates.
     * @see #hasCoords()
     */
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
