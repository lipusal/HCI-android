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

    public static Flight fromJson(JsonObject flightObj) {
        Flight result = new Flight();
        Gson g = new Gson();
        JsonObject outboundRoute = result.getOutboundRoute(flightObj),
                outboundSegment = result.getOutboundSegment(outboundRoute);
        //Parse basic info
        result.status = null;
        result.total = flightObj.getAsJsonObject("price").getAsJsonObject("total").get("total").getAsDouble();
        result.id = outboundSegment.get("id").getAsInt();
        result.number = outboundSegment.get("number").getAsInt();
        result.airline = PersistentData.getInstance().getAirlines().get(outboundSegment.getAsJsonObject("airline").get("id").getAsString());
        result.durationStr = outboundRoute.get("duration").getAsString();
        //Parse departureDate/arrivalDate Airport objects
        result.departureAirport = g.fromJson(outboundSegment.getAsJsonObject("departure").get("airport"), Airport.class);
        result.arrivalAirport = g.fromJson(outboundSegment.getAsJsonObject("arrival").get("airport"), Airport.class);
        //Parse departureDate/arrivalDate dates
        result.departureDate = result.parseDate(outboundSegment.getAsJsonObject("departure").get("date"), result.departureAirport.getTimezoneStr());
        result.arrivalDate = result.parseDate(outboundSegment.getAsJsonObject("arrival").get("date"), result.arrivalAirport.getTimezoneStr());
        return result;
    }

    private JsonObject getOutboundRoute(JsonObject flightObj) {
        return flightObj.getAsJsonArray("outbound_routes").get(0).getAsJsonObject();
    }

    private JsonObject getOutboundSegment(JsonObject routeObj) {
        return routeObj.getAsJsonArray("segments").get(0).getAsJsonObject();
    }

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

    public String getDurationStr() {
        return durationStr;
    }

    public int getNumber() {
        return number;
    }

    public int getID() {
        return id;
    }

    public Airport getDepartureAirport() {
        return departureAirport;
    }

    public Airport getArrivalAirport() {
        return arrivalAirport;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public String getPrettyDepartureDate() {
        return prettyFormat.format(departureDate);
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public String getPrettyArrivalDate() {
        return prettyFormat.format(arrivalDate);
    }

    public FlightStatus getStatus() {
        return status;
    }

    public double getTotal() {
        return total;
    }

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
        return airline.getID() + " #" + number + ", " + departureAirport.getID() + "=>" + arrivalAirport.getID() + " @ " + prettyFormat.format(departureDate) + " (id=" + id + ")";
    }
}
