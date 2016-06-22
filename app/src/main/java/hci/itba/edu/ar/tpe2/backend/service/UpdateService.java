package hci.itba.edu.ar.tpe2.backend.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatusComparator;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;

/**
 * Service used for checking for flight updates in the background. Sends an ordered broadcast when
 * done updating.
 * <br /><br />
 * To check for updates for all currently watched flights, do the following (assuming {@code this}
 * is a subclass of {@link android.content.Context Context}):
 * <pre>
 * {@code
 *   Intent intent = new Intent(this, UpdateService.class);
 *      intent.setAction(UpdateService.ACTION_CHECK_FOR_UPDATES);
 *      this.startService(intent);
 * }
 * </pre>
 */
public class UpdateService extends IntentService {
    public static final String ACTION_CHECK_FOR_UPDATES = "hci.itba.edu.ar.tpe2.backend.service.action.CHECK_FOR_UPDATES",
            ACTION_FLIGHTS_UPDATED = "hci.itba.edu.ar.tpe2.backend.service.action.FLIGHTS_UPDATED";

    /**
     * Extra broadcast parameter, a Map&lt;Integer, FlightStatus&gt; indicating the updated
     * statuses, identified by their flight ID.
     */
    public static final String EXTRA_UPDATES = "hci.itba.edu.ar.tpe2.backend.service.extra.UPDATES";

    /**
     * Extra broadcast parameter, a Map&lt;Integer, Map&lt;FlightStatusComparator.ComparableField, Serializable&gt;&gt;
     * indicating the differences in the updated status with respect to their old version,
     * identified by their flight ID.
     */
    public static final String EXTRA_DIFFERENCES = "hci.itba.edu.ar.tpe2.backend.service.extra.DIFFERENCES";

    public UpdateService() {
        super("UpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_CHECK_FOR_UPDATES:
                    notifyUpdates();
                    break;
            }
        }
    }

    /**
     * Checks for changes in the user's followed flights. If there are updates, writes updates in
     * persistent data.  Sends an ordered broadcast with a Map of new statuses and their differences
     * with respect to the old ones.
     */
    private void notifyUpdates() {
        final PersistentData persistentData = new PersistentData(this);
        final Map<Integer, FlightStatus> statuses = persistentData.getWatchedStatuses();
        if (statuses.size() > 0) {
            Log.d("VOLANDO", "Fetching updates for " + statuses.size() + " watched flights");
        } else {
            Log.d("VOLANDO", "No watched flights, not checking updates");
//            Intent intent = new Intent(ACTION_FLIGHTS_UPDATED);
//            intent.putExtra(EXTRA_UPDATES, (Serializable) Collections.EMPTY_MAP);
//            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            return;
        }

        /**
         * Make an async network request for each flight that needs updates. Update persistent data
         * as needed. When ALL requests complete, broadcast.
         */
        final AtomicInteger requestsLeft = new AtomicInteger(statuses.size());       //To avoid race condition when waiting for all AsyncTasks to complete
        final Map<Integer, FlightStatus> updatedStatuses = new HashMap<>(statuses.size());
        final Map<Integer, Map<FlightStatusComparator.ComparableField, Serializable>> differences = new HashMap<>();
        for (final FlightStatus status : statuses.values()) {
            API.getInstance().getFlightStatus(
                    status.getAirline().getID(),
                    status.getFlight().getNumber(),
                    this,
                    new NetworkRequestCallback<FlightStatus>() {
                        @Override
                        public void execute(Context c, FlightStatus newStatus) {
                            Map<FlightStatusComparator.ComparableField, Serializable> statusDifferences = new FlightStatusComparator(status).compare(newStatus);
                            if (!statusDifferences.isEmpty()) {
                                updatedStatuses.put(newStatus.getFlight().getID(), newStatus);
                                differences.put(newStatus.getFlight().getID(), statusDifferences);
                                persistentData.updateStatus(newStatus);
                                Log.d("VOLANDO", status.getFlight().toString() + " is now " + newStatus.toString());
                            }
                            if (requestsLeft.decrementAndGet() == 0) {   //Decrement and check atomically to avoid race condition
                                //Done, broadcast
                                Intent intent = new Intent(ACTION_FLIGHTS_UPDATED);
                                intent.putExtra(EXTRA_UPDATES, (Serializable) updatedStatuses);
                                intent.putExtra(EXTRA_DIFFERENCES, (Serializable) differences);
                                sendOrderedBroadcast(intent, null);
                            }
                        }
                    },
                    new NetworkRequestCallback<String>() {
                        @Override
                        public void execute(Context c, String param) {
                            Log.w("VOLANDO", "Error getting status updates for " + status.toString() + ":\n" + param);  //TODO notify user? Try again right away? Broadcast? Wait?
                        }
                    });
        }
    }
}
