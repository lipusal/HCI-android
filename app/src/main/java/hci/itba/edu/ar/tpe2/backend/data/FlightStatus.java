package hci.itba.edu.ar.tpe2.backend.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.tz.DateTimeZoneBuilder;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import hci.itba.edu.ar.tpe2.R;

/**
 * Ugly POJO used to hold flight status data.
 */
public class FlightStatus implements Serializable {
    private static DateFormat APIdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ", Locale.US),
            prettyFormat = DateFormat.getDateTimeInstance();
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZZ").withOffsetParsed().withLocale(Locale.getDefault());
    private static final Map<String, String> validStatus = new HashMap<>();
    private static final Map<String, Integer> statusResIDs = new HashMap<>();
    static {
        validStatus.put("S", "scheduled");
        validStatus.put("A", "active");
        validStatus.put("D", "diverted");
        validStatus.put("L", "landed");
        validStatus.put("C", "canceled");

        statusResIDs.put("S", R.string.status_scheduled);
        statusResIDs.put("A", R.string.status_active);
        statusResIDs.put("D", R.string.status_diverted);
        statusResIDs.put("L", R.string.status_landed);
        statusResIDs.put("C", R.string.status_canceled);

    }


    private Flight flight;
    private Airline airline;
    private Airport originAirport, destinationAirport;
    private String status, departureTerminal, arrivalTerminal, departureGate, arrivalGate, baggageClaim;
    private DateTime scheduledDepartureTime, actualDepartureTime, scheduledDepartureGateTime, actualDepartureGateTime, scheduledDepartureRunwayTime, actualDepartureRunwayTime,
            scheduledArrivalTime, actualArrivalTime, scheduledArrivalGateTime, actualArrivalGateTime, scheduledArrivalRunwayTime, actualArrivalRunwayTime;
    //TODO incorporate delays

    private FlightStatus() {
    }

    public static FlightStatus fromJson(JsonObject statusObject) {
        FlightStatus result = new FlightStatus();
        JsonObject departure = statusObject.getAsJsonObject("departure"),
                arrival = statusObject.getAsJsonObject("arrival");
        PersistentData persistentData = PersistentData.getContextLessInstance();
        result.status = statusObject.get("status").getAsString();
        result.airline = persistentData.getAirlines().get(statusObject.getAsJsonObject("airline").get("id").getAsString());
        result.originAirport = persistentData.getAirports().get(departure.getAsJsonObject("airport").get("id").getAsString());
        result.destinationAirport = persistentData.getAirports().get(arrival.getAsJsonObject("airport").get("id").getAsString());
        result.baggageClaim = arrival.getAsJsonObject("airport").get("baggage").isJsonNull() ? null : arrival.getAsJsonObject("airport").get("baggage").getAsString();
        result.parseDeparture(departure);
        result.parseArrival(arrival);
        result.flight = new Flight(
                statusObject.get("id").getAsInt(),
                statusObject.get("number").getAsInt(),
                result.airline
        );
        return result;
    }

    private void parseDeparture(JsonObject departureObject) {
        parseDates(departureObject, "departure");
    }

    private void parseArrival(JsonObject arrivalObject) {
        parseDates(arrivalObject, "arrival");
    }

    private void parseDates(JsonObject obj, String departureOrArrival) {
        JsonObject airport = obj.getAsJsonObject("airport");
        String timezone = airport.get("time_zone").getAsString();
        timezone = (timezone.charAt(0) == '-' ? "" : "+") + timezone;
        if (departureOrArrival.equals("departure")) {
            scheduledDepartureTime = parseDate(obj, "scheduled_time", timezone);
            actualDepartureTime = parseDate(obj, "actual_time", timezone);
            scheduledDepartureGateTime = parseDate(obj, "scheduled_gate_time", timezone);
            actualDepartureGateTime = parseDate(obj, "scheduled_time", timezone);
            scheduledDepartureRunwayTime = parseDate(obj, "estimate_runway_time", timezone);
            actualDepartureRunwayTime = parseDate(obj, "actual_runway_time", timezone);
            departureTerminal = airport.get("terminal").isJsonNull() ? null : airport.get("terminal").getAsString();
            departureGate = airport.get("gate").isJsonNull() ? null : airport.get("gate").getAsString();
        } else if (departureOrArrival.equals("arrival")) {
            scheduledArrivalTime = parseDate(obj, "scheduled_time", timezone);
            actualArrivalTime = parseDate(obj, "actual_time", timezone);
            scheduledArrivalGateTime = parseDate(obj, "scheduled_gate_time", timezone);
            actualArrivalGateTime = parseDate(obj, "scheduled_time", timezone);
            scheduledArrivalRunwayTime = parseDate(obj, "estimate_runway_time", timezone);
            actualArrivalRunwayTime = parseDate(obj, "actual_runway_time", timezone);
            arrivalTerminal = airport.get("terminal").isJsonNull() ? null : airport.get("terminal").getAsString();
            arrivalGate = airport.get("gate").isJsonNull() ? null : airport.get("gate").getAsString();
        }
    }

    private DateTime parseDate(JsonObject arrivalOrDepartureObject, String dateTimeKey, String timezoneStr) {
        Calendar cal = Calendar.getInstance();
        DateTime la;
        try {
            JsonElement date = arrivalOrDepartureObject.get(dateTimeKey);
            if (date.isJsonNull()) {
                return null;
            }
            cal.setTime(APIdateFormat.parse(date.getAsString() + " " + timezoneStr));
            la = dateTimeFormatter.parseDateTime(date.getAsString() + timezoneStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return la;
    }

    public String getStatus() {
        return status;
    }

    public String getDepartureTerminal() {
        return departureTerminal;
    }

    public String getDepartureGate() {
        return departureGate;
    }

    public DateTime getScheduledDepartureTime() {
        return scheduledDepartureTime;
    }

    public String getPrettyScheduledDepartureTime() {
        return prettyFormat.format(scheduledDepartureTime);
    }

//    public DateTime getDepartureDelay() {
//        if(actualDepartureTime == null) {   //Hasn't taken off yet
//            Date now = new Date();
//            if(now.before(scheduledDepartureTime)) {
//                return null;
//            }
//            else {
//                Period period = new Period(scheduledDepartureTime, new DateTime(now, DateTimeZone.UTC));
//            }
//        }
//        else {
//            add delays
//        }
//
//        return null;
//    }

    public DateTime getActualDepartureTime() {
        return actualDepartureTime;
    }

    public String getPrettyActualDepartureTime() {
        return prettyFormat.format(actualDepartureTime);
    }

    public DateTime getScheduledDepartureGateTime() {
        return scheduledDepartureGateTime;
    }

    public String getPrettyScheduledDepartureGateTime() {
        return prettyFormat.format(scheduledDepartureGateTime);
    }

    public DateTime getActualDepartureGateTime() {
        return actualDepartureGateTime;
    }

    public String getPrettyActualDepartureGateTime() {
        return prettyFormat.format(actualDepartureGateTime);
    }

    public DateTime getScheduledDepartureRunwayTime() {
        return scheduledDepartureRunwayTime;
    }

    public String getPrettyScheduledDepartureRunwayTime() {
        return prettyFormat.format(scheduledDepartureRunwayTime);
    }

    public DateTime getActualDepartureRunwayTime() {
        return actualDepartureRunwayTime;
    }

    public String getPrettyActualDepartureRunwayTime() {
        return prettyFormat.format(actualDepartureRunwayTime);
    }

    public String getArrivalTerminal() {
        return arrivalTerminal;
    }

    public String getArrivalGate() {
        return arrivalGate;
    }

    public DateTime getScheduledArrivalTime() {
        return scheduledArrivalTime;
    }

    public String getPrettyScheduledArrivalTime() {
        return prettyFormat.format(scheduledArrivalTime);
    }

    public DateTime getActualArrivalTime() {
        return actualArrivalTime;
    }

    public String getPrettyActualArrivalTime() {
        return prettyFormat.format(actualArrivalTime);
    }

    public DateTime getScheduledArrivalGateTime() {
        return scheduledArrivalGateTime;
    }

    public String getPrettyScheduledArrivalGateTime() {
        return prettyFormat.format(scheduledArrivalGateTime);
    }

    public DateTime getActualArrivalGateTime() {
        return actualArrivalGateTime;
    }

    public String getPrettyActualArrivalGateTime() {
        return prettyFormat.format(actualArrivalGateTime);
    }

    public DateTime getScheduledArrivalRunwayTime() {
        return scheduledArrivalRunwayTime;
    }

    public String getPrettyScheduledArrivalRunwayTime() {
        return prettyFormat.format(scheduledArrivalRunwayTime);
    }

    public DateTime getActualArrivalRunwayTime() {
        return actualArrivalRunwayTime;
    }

    public String getPrettyActualArrivalRunwayTime() {
        return prettyFormat.format(actualArrivalRunwayTime);
    }

    public Airline getAirline() {
        return airline;
    }

    public Flight getFlight() {
        return flight;
    }

    public Airport getOriginAirport() {
        return originAirport;
    }

    public Airport getDestinationAirport() {
        return destinationAirport;
    }

    public String getBaggageClaim() {
        return baggageClaim;
    }

    public int getIconID() {
        switch (status) {
            case "S":
                return R.drawable.ic_scheduled;
            case "A":
                return R.drawable.ic_flight_takeoff_black;
            case "D":
                return R.drawable.ic_diverted;
            case "L":
                return R.drawable.ic_flight_land_black;
            case "C":
                return R.drawable.ic_cancelled;
            default:
                return -1;
        }
    }

    /**
     * Two status objects are considered equal if their Flight objects are considered
     * {@link Flight#equals(Object) equal}.
     *
     * @param o the other Object to compare against.
     * @return Whether the two objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlightStatus that = (FlightStatus) o;
        return this.flight.equals(that.flight);

    }

    @Override
    public String toString() {
        return validStatus.get(status);
    }

    @Override
    public int hashCode() {
        return flight.getID();
    }

    /**
     * Gets the String resource ID corresponding to this status. Makes this status translatable.
     * @return
     */
    public int getStringResID() {
        return statusResIDs.get(status);
    }
}
