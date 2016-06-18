package hci.itba.edu.ar.tpe2.backend.data;

import android.content.Context;

import java.util.Currency;
import java.util.List;
import java.util.Map;

import hci.itba.edu.ar.tpe2.backend.FileManager;

/**
 * Class used to hold persistent data used across various activities.
 */
public class PersistentData {
    private static PersistentData instance = new PersistentData();
    private List<Flight> followedFlights;
    private Map<String, City> cities;
    private Map<String, Country> countries;
    private Map<String, Currency> currencies;
    private Map<String, Airport> airports;
    private Map<String, Airline> airlines;

    public static PersistentData getInstance() {
        return instance;
    }

    private PersistentData() {}

    public List<Flight> getFollowedFlights() {
        return followedFlights;
    }

    public void setFollowedFlights(List<Flight> followedFlights) {
        this.followedFlights = followedFlights;
    }

    public Map<String, City> getCities() {
        return cities;
    }

    public void setCities(Map<String, City> cities) {
        this.cities = cities;
    }

    public Map<String, Country> getCountries() {
        return countries;
    }

    public void setCountries(Map<String, Country> countries) {
        this.countries = countries;
    }

    public Map<String, Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(Map<String, Currency> currencies) {
        this.currencies = currencies;
    }

    public Map<String, Airport> getAirports() {
        return airports;
    }

    public void setAirports(Map<String, Airport> airports) {
        this.airports = airports;
    }

    public Map<String, Airline> getAirlines() {
        return airlines;
    }

    public void setAirlines(Map<String, Airline> airlines) {
        this.airlines = airlines;
    }

    public void addFollowedFlight(Flight f, Context context) {
        if (followedFlights == null) {
            throw new IllegalStateException("Followed flights have not been set, can't add flight.");
        }
        if (followedFlights.contains(f)) {
            throw new IllegalArgumentException("Flight already followed: " + f.toString());
        }
        followedFlights.add(f);
        FileManager fm = new FileManager(context);
        fm.saveFollowedFlights(followedFlights);
    }

    public void removeFollowedFlight(Flight f, Context context) {
        if (followedFlights == null) {
            throw new IllegalStateException("Followed flights have not been set, can't remove flight.");
        }
        followedFlights.remove(f);
        FileManager fm = new FileManager(context);
        fm.saveFollowedFlights(followedFlights);
    }
}
