package hci.itba.edu.ar.tpe2.backend.data;

import java.io.Serializable;

public class Airport extends Place implements Serializable {
    private String id, description, time_zone;
    private City city;

    public Airport(String id, String description, String timezoneStr, City city) {
        this.id = id;
        this.description = description;
        this.time_zone = timezoneStr;
        this.city = city;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName() {
        return description;
    }

    public String getDescription() {
        return description;
    }

    public String getTimezoneStr() {
        return time_zone;
    }

    public City getCity() {
        return city;
    }

    @Override
    public double getLatitude() {
        return city.getLatitude();
    }

    @Override
    public double getLongitude() {
        return city.getLongitude();
    }

    public void setCity(City city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return description + " (" + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Airport airport = (Airport) o;
        return id.equals(airport.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
