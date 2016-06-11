package hci.itba.edu.ar.tpe2.backend.data;

import java.util.Currency;
import java.util.List;
import java.util.Map;

public class PersistentData {
    private static PersistentData instance = new PersistentData();
    private List<Flight> followedFlights;
    private Map<String, City> cities;
    private Map<String, Country> countries;
    private Map<String, Currency> currencies;
    private Map<String, Airport> airports;

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

    //    public void addFollowedFlight(Flight f) {
//        if(followedFlights == null) {
//            throw new IllegalStateException("Followed flights have not been set, can't add flight.");
//        }
//        followedFlights.add(f);
//    }
//
//    public boolean removeFollowedFlight(Flight f) {
//        if(followedFlights == null) {
//            throw new IllegalStateException("Followed flights have not been set, can't remove flight.");
//        }
//        return followedFlights.remove(f);
//    }
}
