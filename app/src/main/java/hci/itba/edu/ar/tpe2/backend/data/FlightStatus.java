package hci.itba.edu.ar.tpe2.backend.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import hci.itba.edu.ar.tpe2.R;

/**
 * Ugly POJO used to hold flight status data.
 */
public class FlightStatus implements Serializable {
    //Date/time formatters
    private static DateFormat APIdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ", Locale.US);
    private static DateTimeFormatter APIDateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZZ").withOffsetParsed().withLocale(Locale.getDefault());
    private static PeriodFormatter delayFormatter = new PeriodFormatterBuilder()
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendHours()
            .appendSeparator(":")
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendMinutes()
            .toFormatter();

    private static final Map<String, Integer> statusResIDs = new HashMap<>();

    static {
        statusResIDs.put("S", R.string.status_scheduled);
        statusResIDs.put("A", R.string.status_active);
        statusResIDs.put("R", R.string.status_diverted);
        statusResIDs.put("L", R.string.status_landed);
        statusResIDs.put("C", R.string.status_canceled);
    }


    private Flight flight;
    private Airline airline;
    private Airport originAirport, destinationAirport;
    private String status, departureTerminal, arrivalTerminal, departureGate, arrivalGate, baggageClaim;
    private DateTime scheduledDepartureTime, actualDepartureTime, scheduledDepartureGateTime, actualDepartureGateTime, scheduledDepartureRunwayTime, actualDepartureRunwayTime,
            scheduledArrivalTime, actualArrivalTime, scheduledArrivalGateTime, actualArrivalGateTime, scheduledArrivalRunwayTime, actualArrivalRunwayTime;
    private Integer originGateDelay, originRunwayDelay, arrivalRunwayDelay, arrivalGateDelay;

    private FlightStatus() {
    }

    public static FlightStatus fromJson(JsonObject statusObject) {
        FlightStatus result = new FlightStatus();
        JsonObject departure = statusObject.getAsJsonObject("departure"),
                arrival = statusObject.getAsJsonObject("arrival");
        PersistentData persistentData = PersistentData.getContextLessInstance();
        result.status = statusObject.get("status").getAsString();
        result.airline = persistentData.getAirlines().get(statusObject.getAsJsonObject("airline").get("id").getAsString());
        result.parseDeparture(departure);
        result.parseArrival(arrival);
        result.baggageClaim = arrival.getAsJsonObject("airport").get("baggage").isJsonNull() ? null : arrival.getAsJsonObject("airport").get("baggage").getAsString();
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
            originAirport = PersistentData.getContextLessInstance().getAirports().get(obj.getAsJsonObject("airport").get("id").getAsString());
            originGateDelay = obj.get("gate_delay").isJsonNull() ? null : obj.get("gate_delay").getAsInt();
            originRunwayDelay = obj.get("runway_delay").isJsonNull() ? null : obj.get("runway_delay").getAsInt();
            scheduledDepartureTime = parseDate(obj, "scheduled_time", timezone);
            actualDepartureTime = parseDate(obj, "actual_time", timezone);
            scheduledDepartureGateTime = parseDate(obj, "scheduled_gate_time", timezone);
            actualDepartureGateTime = parseDate(obj, "scheduled_time", timezone);
            scheduledDepartureRunwayTime = parseDate(obj, "estimate_runway_time", timezone);
            actualDepartureRunwayTime = parseDate(obj, "actual_runway_time", timezone);
            departureTerminal = airport.get("terminal").isJsonNull() ? null : airport.get("terminal").getAsString();
            departureGate = airport.get("gate").isJsonNull() ? null : airport.get("gate").getAsString();
        } else if (departureOrArrival.equals("arrival")) {
            destinationAirport = PersistentData.getContextLessInstance().getAirports().get(obj.getAsJsonObject("airport").get("id").getAsString());
            arrivalGateDelay = obj.get("gate_delay").isJsonNull() ? null : obj.get("gate_delay").getAsInt();
            arrivalRunwayDelay = obj.get("runway_delay").isJsonNull() ? null : obj.get("runway_delay").getAsInt();
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
            la = APIDateTimeFormatter.parseDateTime(date.getAsString() + timezoneStr);
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
        return departureTerminal == null ? "—" : departureTerminal;
    }

    public String getDepartureGate() {
        return departureGate == null ? "—" : departureGate;
    }

    public DateTime getScheduledDepartureTime() {
        return scheduledDepartureTime;
    }

    public String getPrettyScheduledDepartureTime() {
        return localizeDateTime(scheduledDepartureTime);
    }

    public Period getDepartureDelay() {
        if (getStringResID() == R.string.status_canceled) {
            return null;
        } else if (actualDepartureTime == null) {   //Hasn't taken off yet
            DateTime now = DateTime.now();
            if (now.isBefore(scheduledDepartureTime)) {
                return null;
            } else {
                return new Period(scheduledDepartureTime, now);
            }
        } else {
            if (actualDepartureTime.equals(scheduledDepartureTime)) {
                return null;
            } else {
                long delayMinutes = (originGateDelay == null ? 0 : originGateDelay) + (originRunwayDelay == null ? 0 : originRunwayDelay);
                return new Period(delayMinutes * 60 * 1000);
            }
        }
    }

    public String getPrettyDepartureDelay() {
        Period delay = getDepartureDelay();
        return (delay == null || delay.equals(new Period())) ? "—" : delayFormatter.print(delay);   //Empty period
    }

    public DateTime getActualDepartureTime() {
        return actualDepartureTime;
    }

    public String getPrettyActualDepartureTime() {
        return actualDepartureTime == null ? "—" : localizeDateTime(actualDepartureTime);
    }

    public DateTime getScheduledDepartureGateTime() {
        return scheduledDepartureGateTime;
    }

    public String getPrettyScheduledDepartureGateTime() {
        return scheduledDepartureGateTime == null ? "—" : localizeDateTime(scheduledDepartureGateTime);
    }

    public DateTime getActualDepartureGateTime() {
        return actualDepartureGateTime;
    }

    public String getPrettyActualDepartureGateTime() {
        return actualDepartureGateTime == null ? "—" : localizeDateTime(actualDepartureGateTime);
    }

    public DateTime getScheduledDepartureRunwayTime() {
        return scheduledDepartureRunwayTime;
    }

    public String getPrettyScheduledDepartureRunwayTime() {
        return scheduledDepartureRunwayTime == null ? "—" : localizeDateTime(scheduledDepartureRunwayTime);
    }

    public DateTime getActualDepartureRunwayTime() {
        return actualDepartureRunwayTime;
    }

    public String getPrettyActualDepartureRunwayTime() {
        return actualDepartureRunwayTime == null ? "—" : localizeDateTime(actualDepartureRunwayTime);
    }

    public String getArrivalTerminal() {
        return arrivalTerminal == null ? "—" : arrivalTerminal;
    }

    public String getArrivalGate() {
        return arrivalGate == null ? "—" : arrivalGate;
    }

    public DateTime getScheduledArrivalTime() {
        return scheduledArrivalTime;
    }

    public String getPrettyScheduledArrivalTime() {
        return localizeDateTime(scheduledArrivalTime);
    }

    public DateTime getActualArrivalTime() {
        return actualArrivalTime;
    }

    public String getPrettyActualArrivalTime() {
        return actualArrivalTime == null ? "—" : localizeDateTime(actualArrivalTime);
    }

    public Period getArrivalDelay() {
        if (getStringResID() == R.string.status_canceled) {
            return null;
        } else if (actualArrivalTime == null) {   //Hasn't landed yet
            DateTime now = DateTime.now();
            if (now.isBefore(scheduledArrivalTime)) {
                return null;
            } else {
                return new Period(scheduledArrivalTime, now);
            }
        } else {
            if (actualArrivalTime.equals(scheduledArrivalTime)) {
                return null;
            } else {
                long delayMinutes = (arrivalGateDelay == null ? 0 : arrivalGateDelay) + (arrivalRunwayDelay == null ? 0 : arrivalRunwayDelay);
                return new Period(delayMinutes * 60 * 1000);
            }
        }
    }

    public String getPrettyArrivalDelay() {
        Period delay = getArrivalDelay();
        return (delay == null || delay.equals(new Period())) ? "—" : delayFormatter.print(delay);    //Empty period
    }

    public DateTime getScheduledArrivalGateTime() {
        return scheduledArrivalGateTime;
    }

    public String getPrettyScheduledArrivalGateTime() {
        return scheduledArrivalGateTime == null ? "—" : localizeDateTime(scheduledArrivalGateTime);
    }

    public DateTime getActualArrivalGateTime() {
        return actualArrivalGateTime;
    }

    public String getPrettyActualArrivalGateTime() {
        return actualArrivalGateTime == null ? "—" : localizeDateTime(actualArrivalGateTime);
    }

    public DateTime getScheduledArrivalRunwayTime() {
        return scheduledArrivalRunwayTime;
    }

    public String getPrettyScheduledArrivalRunwayTime() {
        return scheduledArrivalRunwayTime == null ? "—" : localizeDateTime(scheduledArrivalRunwayTime);
    }

    public DateTime getActualArrivalRunwayTime() {
        return actualArrivalRunwayTime;
    }

    public String getPrettyActualArrivalRunwayTime() {
        return actualArrivalRunwayTime == null ? "—" : localizeDateTime(actualArrivalRunwayTime);
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
        return baggageClaim == null ? "—" : baggageClaim;
    }

    public Integer getOriginGateDelay() {
        return originGateDelay;
    }

    public Integer getOriginRunwayDelay() {
        return originRunwayDelay;
    }

    public Integer getArrivalRunwayDelay() {
        return arrivalRunwayDelay;
    }

    public Integer getArrivalGateDelay() {
        return arrivalGateDelay;
    }

    public int getIconID() {
        switch (status) {
            case "S":
                return R.drawable.ic_scheduled;
            case "A":
                return R.drawable.ic_flight_takeoff_black;
            case "R":
                return R.drawable.ic_diverted;
            case "L":
                return R.drawable.ic_flight_land_black;
            case "C":
                return R.drawable.ic_cancelled;
            default:
                return -1;
        }
    }

    private String localizeDateTime(DateTime date) {
        String full = DateTimeFormat.shortDateTime().print(date);
        return full.replaceFirst(" ", "\n");
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
    public int hashCode() {
        return flight.getID();
    }

    /**
     * Gets the String resource ID corresponding to this status. Makes this status translatable.
     *
     * @return
     */
    public int getStringResID() {
        return statusResIDs.get(status);
    }
}
