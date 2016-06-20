package hci.itba.edu.ar.tpe2.backend.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Flight implements Serializable {
    private static DateFormat APIdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ", Locale.US),
            prettyFormat = DateFormat.getDateTimeInstance();

    private String durationStr;
    private int number, id;
    private Airline airline;
    private Airport departureAirport, arrivalAirport;
    private Date departureDate, arrivalDate;
    private FlightStatus status;
    private double total;

    private Flight() {
    }

    /**
     * Constructs a new Flight with the specified information.
     *
     * @param id
     * @param number
     * @param airline
     */
    public Flight(int id, int number, Airline airline) {
        this.id = id;
        this.number = number;
        this.airline = airline;
    }

    /**
     * Instantiates a Flight object with the provided JSON data.
     *
     * @param flightObj
     * @return
     */
    public static Flight fromJson(JsonObject flightObj) {
        Flight result = new Flight();
        Gson g = new Gson();
        PersistentData persistentData = PersistentData.getContextLessInstance();
        JsonObject outboundRoute = result.getOutboundRoute(flightObj),
                outboundSegment = result.getOutboundSegment(outboundRoute);
        //Parse basic info
        result.status = null;
        result.total = flightObj.getAsJsonObject("price").getAsJsonObject("total").get("total").getAsDouble();
        result.id = outboundSegment.get("id").getAsInt();
        result.number = outboundSegment.get("number").getAsInt();
        result.airline = persistentData.getAirlines().get(outboundSegment.getAsJsonObject("airline").get("id").getAsString());
        result.durationStr = outboundRoute.get("duration").getAsString();
        //Parse departureDate/arrivalDate Airport objects
        result.departureAirport = persistentData.getAirports().get(outboundSegment.getAsJsonObject("departure").getAsJsonObject("airport").get("id").getAsString());
        result.arrivalAirport = persistentData.getAirports().get(outboundSegment.getAsJsonObject("arrival").getAsJsonObject("airport").get("id").getAsString());
        //Parse departureDate/arrivalDate dates
        result.departureDate = result.parseDate(outboundSegment.getAsJsonObject("departure").get("date"), result.departureAirport.getTimezoneStr());
        result.arrivalDate = result.parseDate(outboundSegment.getAsJsonObject("arrival").get("date"), result.arrivalAirport.getTimezoneStr());
        return result;
    }

    @Deprecated
    private JsonObject getOutboundRoute(JsonObject flightObj) {
        return flightObj.getAsJsonArray("outbound_routes").get(0).getAsJsonObject();
    }

    @Deprecated
    private JsonObject getOutboundSegment(JsonObject routeObj) {
        return routeObj.getAsJsonArray("segments").get(0).getAsJsonObject();
    }

    @Deprecated
    private Date parseDate(JsonElement dateObj, String timezoneStr) {
        Date result;
        try {
            if (dateObj.isJsonNull()) {
                return null;
            }
            result = APIdateFormat.parse(dateObj.getAsString() + " " + timezoneStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public Airline getAirline() {
        return airline;
    }

    @Deprecated
    public String getDurationStr() {
        return durationStr;
    }

    public int getNumber() {
        return number;
    }

    public int getID() {
        return id;
    }

    @Deprecated
    public Airport getDepartureAirport() {
        return departureAirport;
    }

    @Deprecated
    public Airport getArrivalAirport() {
        return arrivalAirport;
    }

    @Deprecated
    public Date getDepartureDate() {
        return departureDate;
    }

    @Deprecated
    public String getPrettyDepartureDate() {
        return prettyFormat.format(departureDate);
    }

    @Deprecated
    public Date getArrivalDate() {
        return arrivalDate;
    }

    @Deprecated
    public String getPrettyArrivalDate() {
        return prettyFormat.format(arrivalDate);
    }

    @Deprecated
    public FlightStatus getStatus() {
        return status;
    }

    @Deprecated
    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    @Deprecated
    public double getTotal() {
        return total;
    }

    /**
     * Two Flights are considered equal if they have the same ID and departure date.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Flight flight = (Flight) o;
        if (id != flight.id) return false;
        return departureDate != null ? departureDate.equals(flight.departureDate) : flight.departureDate == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (departureDate != null ? departureDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return airline.getID() + " #" + number;
    }
}
