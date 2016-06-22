package hci.itba.edu.ar.tpe2.backend.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.util.Collection;
import java.util.Map;

import hci.itba.edu.ar.tpe2.FlightDetailMainActivity;
import hci.itba.edu.ar.tpe2.FlightsActivity;
import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;

public class UpdatePriorityReceiver extends BroadcastReceiver {
    protected View destinationView;

    /**
     * @param destinationView View in which to show the snackbar. If your activity has a FAB, it
     *                        will be within a {@link CoordinatorLayout}. Pass it here as a parameter
     *                        so the snackbar moves the FAB up properly.
     */
    public UpdatePriorityReceiver(View destinationView) {
        this.destinationView = destinationView;
    }

    /**
     * Reacts to flights being updated. Updates the statuses in persistent data and calls
     * {@link #onNoFlightsChanged()}, {@link #onSingleFlightChanged(FlightStatus)} or
     * {@link #onMultipleFlightsChanged(Collection)} as appropriate.
     *
     * @param context Received broadcast context.
     * @param intent  Received broadcast intent.
     */
    @Override
    public final void onReceive(final Context context, Intent intent) {
        abortBroadcast();   //So notification isn't sent
        Log.d("VOLANDO", "Broadcast aborted: " + getAbortBroadcast());
        Map<Integer, FlightStatus> updatedStatuses = (Map<Integer, FlightStatus>) intent.getSerializableExtra(UpdateService.EXTRA_UPDATES);
        if (updatedStatuses.isEmpty()) {
            onNoFlightsChanged();
            return;
        }
        //Find the old statuses and update them
        FlightStatus newStatus = null;
        for (Map.Entry<Integer, FlightStatus> entry : updatedStatuses.entrySet()) {
            newStatus = entry.getValue();
        }
        if (updatedStatuses.size() == 1) {      //newStatus has the only status in the Map
            onSingleFlightChanged(newStatus);
        } else {
            onMultipleFlightsChanged(updatedStatuses.values());
        }
    }

    /**
     * Called when no flights were updated.
     */
    public void onNoFlightsChanged() {
        //Do nothing
    }

    /**
     * Called when exactly 1 flight was updated.
     *
     * @param newStatus The updated flight status.
     */
    public void onSingleFlightChanged(FlightStatus newStatus) {
        showSnackbar(newStatus);
    }

    /**
     * Called when more than 1 flight was updated.
     *
     * @param newStatuses The updated flight statuses.
     */
    public void onMultipleFlightsChanged(Collection<FlightStatus> newStatuses) {

    }

    /**
     * Shows a Snackbar notifying the flight that was updated. Includes an action to see details of
     * said flight.
     *
     * @param newStatus The new status.
     */
    public void showSnackbar(final FlightStatus newStatus) {
        final Context context = destinationView.getContext();
        Snackbar.make(destinationView, newStatus.getFlight().toString() + " " + newStatus.toString(), Snackbar.LENGTH_LONG)
                .setAction(
                        R.string.action_view,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(context, FlightDetailMainActivity.class);
                                i.putExtra(FlightDetailMainActivity.PARAM_STATUS, newStatus);
                                //TODO flags?
                                context.startActivity(i);
                            }
                        })
                .show();
    }

    /**
     * Shows a Snackbar notifying that {@code numUpdates} flights were updated. Includes an action
     * to go to the Flights activity.
     *
     * @param numUpdates The number of updated flights.
     */
    public void showSnackbar(int numUpdates) {
        final Context context = destinationView.getContext();
        Snackbar.make(destinationView, numUpdates + " flights updated", Snackbar.LENGTH_LONG)    //TODO use string resource with placeholder
                .setAction(
                        R.string.action_view,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(context, FlightsActivity.class);
                                //TODO flags?
                                context.startActivity(i);
                            }
                        })
                .show();
    }
}
