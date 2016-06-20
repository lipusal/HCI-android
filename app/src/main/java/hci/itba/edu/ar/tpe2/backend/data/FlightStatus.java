package hci.itba.edu.ar.tpe2.backend.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Ugly POJO used to hold flight status data.
 */
public class FlightStatus implements Serializable {
    private static DateFormat APIdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ", Locale.US),
            prettyFormat = DateFormat.getDateTimeInstance();
    private static final Map<String, String> validStatus = new HashMap<>();

    static {
        validStatus.put("S", "scheduled");
        validStatus.put("A", "active");
        validStatus.put("D", "diverted");
        validStatus.put("L", "landed");
        validStatus.put("C", "canceled");
    }

    private String status, departureTerminal, arrivalTerminal, departureGate, arrivalGate, baggageClaim;
    private Date scheduledDepartureTime, actualDepartureTime, scheduledDepartureGateTime, actualDepartureGateTime, scheduledDepartureRunwayTime, actualDepartureRunwayTime,
            scheduledArrivalTime, actualArrivalTime, scheduledArrivalGateTime, actualArrivalGateTime, scheduledArrivalRunwayTime, actualArrivalRunwayTime;
    private Integer flightId, flightNumber;
    private Airline airline;
    private Airport destinationAirport;     //Can change from original if diverted
    //TODO incorporate delays

    private FlightStatus() {
    }

    public static FlightStatus fromJson(JsonObject statusObject) {
        FlightStatus result = new FlightStatus();
        JsonObject departure = statusObject.getAsJsonObject("departure"),
                arrival = statusObject.getAsJsonObject("arrival");
        PersistentData persistentData = PersistentData.getContextLessInstance();
        result.status = statusObject.get("status").getAsString();
        result.flightId = statusObject.get("id").getAsInt();
        result.flightNumber = statusObject.get("number").getAsInt();
        result.airline = persistentData.getAirlines().get(statusObject.getAsJsonObject("airline").get("id").getAsString());
        result.destinationAirport = persistentData.getAirports().get(departure.getAsJsonObject("airport").get("id").getAsString());
        result.baggageClaim = arrival.getAsJsonObject("airport").get("baggage").isJsonNull() ? null : arrival.getAsJsonObject("airport").get("baggage").getAsString();
        result.parseDeparture(departure);
        result.parseArrival(arrival);
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

    private Date parseDate(JsonObject arrivalOrDepartureObject, String dateTimeKey, String timezoneStr) {
        Date result;
        try {
            JsonElement date = arrivalOrDepartureObject.get(dateTimeKey);
            if (date.isJsonNull()) {
                return null;
            }
            result = APIdateFormat.parse(date.getAsString() + " " + timezoneStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return result;
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

    public Date getScheduledDepartureTime() {
        return scheduledDepartureTime;
    }

    public String getPrettyScheduledDepartureTime() {
        return prettyFormat.format(scheduledDepartureTime);
    }

    public Date getActualDepartureTime() {
        return actualDepartureTime;
    }

    public String getPrettyActualDepartureTime() {
        return prettyFormat.format(actualDepartureTime);
    }

    public Date getScheduledDepartureGateTime() {
        return scheduledDepartureGateTime;
    }

    public String getPrettyScheduledDepartureGateTime() {
        return prettyFormat.format(scheduledDepartureGateTime);
    }

    public Date getActualDepartureGateTime() {
        return actualDepartureGateTime;
    }

    public String getPrettyActualDepartureGateTime() {
        return prettyFormat.format(actualDepartureGateTime);
    }

    public Date getScheduledDepartureRunwayTime() {
        return scheduledDepartureRunwayTime;
    }

    public String getPrettyScheduledDepartureRunwayTime() {
        return prettyFormat.format(scheduledDepartureRunwayTime);
    }

    public Date getActualDepartureRunwayTime() {
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

    public Date getScheduledArrivalTime() {
        return scheduledArrivalTime;
    }

    public String getPrettyScheduledArrivalTime() {
        return prettyFormat.format(scheduledArrivalTime);
    }

    public Date getActualArrivalTime() {
        return actualArrivalTime;
    }

    public String getPrettyActualArrivalTime() {
        return prettyFormat.format(actualArrivalTime);
    }

    public Date getScheduledArrivalGateTime() {
        return scheduledArrivalGateTime;
    }

    public String getPrettyScheduledArrivalGateTime() {
        return prettyFormat.format(scheduledArrivalGateTime);
    }

    public Date getActualArrivalGateTime() {
        return actualArrivalGateTime;
    }

    public String getPrettyActualArrivalGateTime() {
        return prettyFormat.format(actualArrivalGateTime);
    }

    public Date getScheduledArrivalRunwayTime() {
        return scheduledArrivalRunwayTime;
    }

    public String getPrettyScheduledArrivalRunwayTime() {
        return prettyFormat.format(scheduledArrivalRunwayTime);
    }

    public Date getActualArrivalRunwayTime() {
        return actualArrivalRunwayTime;
    }

    public String getPrettyActualArrivalRunwayTime() {
        return prettyFormat.format(actualArrivalRunwayTime);
    }

    public Airline getAirline() {
        return airline;
    }

    public Integer getFlightId() {
        return flightId;
    }

    public Integer getFlightNumber() {
        return flightNumber;
    }

    public Airport getDestinationAirport() {
        return destinationAirport;
    }

    public String getBaggageClaim() {
        return baggageClaim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlightStatus that = (FlightStatus) o;
        return status.equals(that.status);

    }

    @Override
    public String toString() {
        return validStatus.get(status);
    }
}
