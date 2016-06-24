package hci.itba.edu.ar.tpe2.backend.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to compare differences between two FlightStatus objects to report updates. <b>NOTE:</b>
 * This class does not attempt to implement {@link java.util.Comparator}.
 */
public class FlightStatusComparator {
    public enum ComparableField {
        DEPARTURE_TIME, DEPARTURE_TERMINAL, DEPARTURE_GATE,
        ARRIVAL_TIME, ARRIVAL_TERMINAL, ARRIVAL_GATE, BAGGAGE_CLAIM, ARRIVAL_AIRPORT,
        STATUS
    }

    private FlightStatus originalStatus;

    public FlightStatusComparator(FlightStatus originalStatus) {
        if (originalStatus == null) {
            throw new IllegalArgumentException("Can't instantiate FlightStatusComparator with a null original status");
        }
        this.originalStatus = originalStatus;
    }

    /**
     * Collects the differences between the original flight status this comparator was created with
     * and the specified status. <b>WARNING:</b> The specified status is considered "newer" and if
     * differences exist, the map will contain the values of {@code newStatus} rather than the
     * original status. So if, for example, a field is now null, the map will have {@code null} as
     * the "new" value.
     *
     * @param newStatus The newer status to compare the original status with. If differences exist,
     *                  the values of this status will be put in the resulting map.
     * @return A map of the calculated differences, identified by {@link ComparableField} keys. If
     * {@code newStatus} is null, an empty map will be returned.
     */
    public Map<ComparableField, Serializable> compare(FlightStatus newStatus) {
        if (newStatus == null) {
            return Collections.emptyMap();
        }
        Map<ComparableField, Serializable> result = new HashMap<>();
        //Status
        if (!originalStatus.getStatus().equals(newStatus.getStatus())) { //Contemplates null (left side should never be null)
            result.put(ComparableField.STATUS, newStatus.getStatus());
        }
        //Times - if actual departure date is different than scheduled time, add a delay field.
//        if (newStatus.getActualDepartureTime() != null
//                && !originalStatus.getScheduledDepartureTime().equals(newStatus.getActualDepartureTime())) {
//            result.put(ComparableField.DEPARTURE_TIME, newStatus.getActualDepartureTime());
//        }
//        if (newStatus.getActualArrivalTime() != null
//                && !originalStatus.getScheduledArrivalTime().equals(newStatus.getActualArrivalTime())) {
//            result.put(ComparableField.ARRIVAL_TIME, newStatus.getActualArrivalTime());
//        }
        //Terminals and gates
        if ((originalStatus.getDepartureTerminal() == null && newStatus.getDepartureTerminal() != null)
                || (originalStatus.getDepartureTerminal() != null && !originalStatus.getDepartureTerminal().equals(newStatus.getDepartureTerminal()))) {
            result.put(ComparableField.DEPARTURE_TERMINAL, newStatus.getDepartureTerminal());
        }
        if ((originalStatus.getDepartureGate() == null && newStatus.getDepartureGate() != null)
                || (originalStatus.getDepartureGate() != null && !originalStatus.getDepartureGate().equals(newStatus.getDepartureGate()))) {
            result.put(ComparableField.DEPARTURE_GATE, newStatus.getDepartureGate());
        }
        if ((originalStatus.getArrivalTerminal() == null && newStatus.getArrivalTerminal() != null)
                || (originalStatus.getArrivalTerminal() != null && !originalStatus.getArrivalTerminal().equals(newStatus.getArrivalTerminal()))) {
            result.put(ComparableField.ARRIVAL_TERMINAL, newStatus.getArrivalTerminal());
        }
        if ((originalStatus.getArrivalGate() == null && newStatus.getArrivalGate() != null)
                || (originalStatus.getArrivalGate() != null && !originalStatus.getArrivalGate().equals(newStatus.getArrivalGate()))) {
            result.put(ComparableField.ARRIVAL_GATE, newStatus.getArrivalGate());
        }
        //Baggage claim
        if ((originalStatus.getBaggageClaim() == null && newStatus.getBaggageClaim() != null)
                || (originalStatus.getBaggageClaim() != null && !originalStatus.getBaggageClaim().equals(newStatus.getBaggageClaim()))) {
            result.put(ComparableField.BAGGAGE_CLAIM, newStatus.getBaggageClaim());
        }
        //Airport (i.e. if diverted)
        if (originalStatus.getDestinationAirport() != null
                && newStatus.getDestinationAirport() != null
                && !originalStatus.getDestinationAirport().equals(newStatus.getDestinationAirport())) {
            result.put(ComparableField.ARRIVAL_AIRPORT, PersistentData.getContextLessInstance().getAirports().get(newStatus.getDestinationAirport().getID()));
        }
        return result;
    }
}
