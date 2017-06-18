package hci.itba.edu.ar.tpe2.backend.data;

/**
 * Class used to generalize behavior common to cities and airports.
 */
public abstract class Place {
    public abstract double getLatitude();
    public abstract double getLongitude();
    public abstract String getID();
    public abstract String getName();
}
