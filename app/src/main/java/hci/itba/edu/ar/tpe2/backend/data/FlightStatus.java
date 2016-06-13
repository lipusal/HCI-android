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
        validStatus.put("S", "Scheduled");
        validStatus.put("A", "Active");
        validStatus.put("D", "Diverted");
        validStatus.put("L", "Landed");
        validStatus.put("C", "Canceled");
    }

    private String status, departureTerminal, arrivalTerminal, departureGate, arrivalGate, airlineName;
    private Date scheduledDepartureTime, actualDepartureTime, scheduledDepartureGateTime, actualDepartureGateTime, scheduledDepartureRunwayTime, actualDepartureRunwayTime,
            scheduledArrivalTime, actualArrivalTime, scheduledArrivalGateTime, actualArrivalGateTime, scheduledArrivalRunwayTime, actualArrivalRunwayTime;
    private Integer flightId, flightNumber;
    //TODO incorporate delays

    private FlightStatus() {
    }

    public static FlightStatus fromJson(JsonObject statusObject) {
        FlightStatus result = new FlightStatus();
        JsonObject departure = statusObject.getAsJsonObject("departure"),
                arrival = statusObject.getAsJsonObject("arrival");
        result.status = statusObject.get("status").getAsString();
        result.flightId = statusObject.get("id").getAsInt();
        result.flightNumber = statusObject.get("number").getAsInt();
        result.airlineName = statusObject.getAsJsonObject("airline").get("name").getAsString();
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
            departureTerminal = airport.get("terminal").isJsonNull() ? null : obj.get("terminal").getAsString();
            departureGate = airport.get("gate").isJsonNull() ? null : obj.get("gate").getAsString();
        } else if (departureOrArrival.equals("arrival")) {
            scheduledArrivalTime = parseDate(obj, "scheduled_time", timezone);
            actualArrivalTime = parseDate(obj, "actual_time", timezone);
            scheduledArrivalGateTime = parseDate(obj, "scheduled_gate_time", timezone);
            actualArrivalGateTime = parseDate(obj, "scheduled_time", timezone);
            scheduledArrivalRunwayTime = parseDate(obj, "estimate_runway_time", timezone);
            actualArrivalRunwayTime = parseDate(obj, "actual_runway_time", timezone);
            arrivalTerminal = airport.get("terminal").isJsonNull() ? null : obj.get("terminal").getAsString();
            arrivalGate = airport.get("gate").isJsonNull() ? null : obj.get("gate").getAsString();
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

    public Date getActualDepartureTime() {
        return actualDepartureTime;
    }

    public Date getScheduledDepartureGateTime() {
        return scheduledDepartureGateTime;
    }

    public Date getActualDepartureGateTime() {
        return actualDepartureGateTime;
    }

    public Date getScheduledDepartureRunwayTime() {
        return scheduledDepartureRunwayTime;
    }

    public Date getActualDepartureRunwayTime() {
        return actualDepartureRunwayTime;
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

    public Date getActualArrivalTime() {
        return actualArrivalTime;
    }

    public Date getScheduledArrivalGateTime() {
        return scheduledArrivalGateTime;
    }

    public Date getActualArrivalGateTime() {
        return actualArrivalGateTime;
    }

    public Date getScheduledArrivalRunwayTime() {
        return scheduledArrivalRunwayTime;
    }

    public Date getActualArrivalRunwayTime() {
        return actualArrivalRunwayTime;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public Integer getFlightId() {
        return flightId;
    }

    public Integer getFlightNumber() {
        return flightNumber;
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
