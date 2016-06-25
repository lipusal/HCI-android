package hci.itba.edu.ar.tpe2.fragment;

import android.view.View;

import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;

/**
 * Interface for reacting to interactions with the stars in flights lists.
 */
public interface StarInterface {

    /**
     * Called after a flight status was unstarred.
     *
     * @param status The unstarred flight status.
     */
    void onFlightUnstarred(FlightStatus status);

    public FlightStatus getFlightStatus() ;

    public void setFlightStatus(FlightStatus newFlightStatus);

    @Deprecated
    public View getSelectedView();

    @Deprecated
    public void setSelectedView(View newSelectedView);
}

