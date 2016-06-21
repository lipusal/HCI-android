package hci.itba.edu.ar.tpe2.backend.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import hci.itba.edu.ar.tpe2.FlightDetailMainActivity;
import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatusComparator;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;
import hci.itba.edu.ar.tpe2.backend.network.APIRequest;

/**
 * Service used for checking for flight updates in the background. Will notify the user if there has
 * been a change in the specified flight's status.
 * <br /><br />
 * To check for updates for all currently followed flights, do the following (assuming {@code this}
 * is a subclass of {@link android.content.Context Context}):
 * <pre>
 * {@code
 *   Intent intent = new Intent(this, NotificationService.class);
 *      intent.setAction(NotificationService.ACTION_NOTIFY_UPDATES);
 *      this.startService(intent);
 * }
 * </pre>
 */
public class NotificationService extends IntentService {
    public static final String ACTION_NOTIFY_UPDATES = "hci.itba.edu.ar.tpe2.backend.service.action.NOTIFY_UPDATES",
            ACTION_FLIGHTS_UPDATED = "hci.itba.edu.ar.tpe2.backend.service.action.UPDATES_COMPLETE",
            PARAM_BROADCAST_WHEN_COMPLETE = "hci.itba.edu.ar.tpe2.backend.service.param.BROADCAST_WHEN_COMPLETE",
            EXTRA_CHANGED_FLIGHT_IDS = "hci.itba.edu.ar.tpe2.backend.service.extra.CHANGED_FLIGHT_IDS";

    public NotificationService() { super("NotificationService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            switch(intent.getAction()) {
                case ACTION_NOTIFY_UPDATES:
                    boolean broadcast = intent.getBooleanExtra(PARAM_BROADCAST_WHEN_COMPLETE, false);
                    notifyUpdates(broadcast);
                    break;
            }
        }
    }

    /**
     * Checks for changes in the user's followed flights, and sends notifications for any flights
     * whose status changed.
     * @param broadcastOnComplete Whether to send a broadcast when done checking for updates.
     */
    private void notifyUpdates(final boolean broadcastOnComplete) {
        final FileManager fileManager = new FileManager(this);
        final List<Flight> flights = fileManager.loadFollowedFlights();
        if(flights.size() > 0) {
            Log.d("VOLANDO", "Fetching updates for " + flights.size() + " followed flights");
        }
        else {
            Log.d("VOLANDO", "No followed flights, not checking updates");
            if (broadcastOnComplete) {
                Intent intent = new Intent(ACTION_FLIGHTS_UPDATED);
                intent.putExtra(EXTRA_CHANGED_FLIGHT_IDS, (Serializable) Collections.EMPTY_SET);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
            return;
        }
        final Gson g = new Gson();
        final AtomicInteger requestsLeft = new AtomicInteger(flights.size());       //To avoid race condition when waiting for all AsyncTasks to complete
        final Map<Integer, Notification> notifications = new HashMap<>(flights.size());
        final Boolean[] changed = {false};
        //Make an async network request for each flight that needs updates. Send all notifications at
        //the same time once ALL updates have been completed.
        for (final FlightStatus status : statuses) {
            API.getInstance().getFlightStatus(
                    status.getAirline().getID(),
                    status.getFlight().getNumber(),
                    this,
                    new NetworkRequestCallback<FlightStatus>() {
                        @Override
                        public void execute(Context c, FlightStatus newStatus) {
                            boolean sendNotifications = PreferenceManager.getDefaultSharedPreferences(NotificationService.this).getBoolean(getString(R.string.pref_key_notify_on_update), Boolean.parseBoolean(getString(R.string.pref_default_notify_on_update)));
                            //Calculate differences and build notifications as appropriate
                            Map<FlightStatusComparator.ComparableField, Object> statusDifferences = new FlightStatusComparator(status).compare(newStatus);
                            if (!statusDifferences.isEmpty()) {
                                Log.d("VOLANDO", "Status changed for " + status.getFlight().toString());
                                persistentData.updateStatus(status, newStatus, NotificationService.this);
                                if (sendNotifications) {
                                    notifications.put(status.getFlight().getID(), buildNotification(status, statusDifferences));
                                }
                                changed[0] = true;
                            }
                            //Last update finished?
                            if (requestsLeft.decrementAndGet() == 0) {   //Decrement and check atomically to avoid race condition
                                if (changed[0]) {
                                    //All updates completed, send all notifications at the same time
                                    NotificationManager notifManager = (NotificationManager) NotificationService.this.getSystemService(NOTIFICATION_SERVICE);
                                    for (Map.Entry<Integer, Notification> entry : notifications.entrySet()) {
                                        notifManager.notify(entry.getKey(), entry.getValue());
                                    }
                                }
                                if (broadcastOnComplete) {
                                    Intent intent = new Intent(ACTION_FLIGHTS_UPDATED);
                                    intent.putExtra(EXTRA_CHANGED_FLIGHT_IDS, (Serializable) notifications.keySet());
                                    LocalBroadcastManager.getInstance(NotificationService.this).sendBroadcast(intent);
                                }
                            }
                        }
                    },
                    new NetworkRequestCallback<String>() {
                        @Override
                        public void execute(Context c, String param) {
                            Log.w("VOLANDO", "Error getting status updates for " + status.toString() + ":\n" + param);  //TODO notify user? Try again right away? Wait?
                        }
                    });
        }
    }

    /**
     * Builds a notification with the appropriate information for the new status of the specified
     * flight. Will notify about the fields present in {@code differences}.
     *
     * @param updatedFlight The flight that was updated. <b>NOTE:</b> {@code updatedFlight.getStatus()}
     *                      should return the updated status.
     * @param differences   A map with the fields of the updated fields, obtained via a
     *                      {@link FlightStatusComparator}. Should not be empty.
     * @return An appropriate notification.
     */
    private Notification buildNotification(Flight updatedFlight, Map<FlightStatusComparator.ComparableField, Object> differences) {
        //TODO provide big icon (i.e. for Moto X active display)
        //TODO group notifications for various updates of the same flight
        //TODO if the user so desires, group all notifications of the app into one
        FlightStatus newStatus = updatedFlight.getStatus();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Build the base notification
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)                                                //Dismiss notification when clicking
                .setCategory(Notification.CATEGORY_STATUS)
                .setPriority(Notification.PRIORITY_HIGH)                            //Trigger heads-up
                //This mouthful gets the sound as set in settings, and falls back to default notification sound if not found TODO not working, plays no sound
                .setSound(Uri.parse(preferences.getString(NotificationService.this.getString(R.string.pref_key_notification_ringtone), NotificationService.this.getString(R.string.pref_default_ringtone))));
        //Set extra parameters
        if (preferences.getBoolean(NotificationService.this.getString(R.string.pref_key_vibrate_on_notify), Boolean.parseBoolean(getString(R.string.pref_default_vibrate_on_notify)))) {
            notifBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }
        //Build content
        if (differences.size() > 1) {
            Log.d("VOLANDO", "Multiple differences for " + updatedFlight.toString() + ": " + differences.entrySet().toString());
            notifBuilder.setContentTitle(updatedFlight.toString() + " status changed");
            notifBuilder.setContentText("Tap to see all changes");
            notifBuilder.setSmallIcon(R.drawable.ic_flight);
        } else {
            for (Map.Entry<FlightStatusComparator.ComparableField, Object> difference : differences.entrySet()) {    //Should only run once, but it's the only way to iterate over a set
                switch (difference.getKey()) {
                    case STATUS:
                        buildNotificationForSatatusChange(notifBuilder, updatedFlight);
                        break;
                    case ARRIVAL_GATE:
                        notifBuilder.setContentTitle(updatedFlight.toString() + " arrival gate is now " + updatedFlight.getStatus().getArrivalGate());    //TODO use string resources for this
                        notifBuilder.setContentText("Arrives at " + updatedFlight.getPrettyArrivalDate());
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_land);
                        break;
                    case ARRIVAL_TERMINAL:
                        notifBuilder.setContentTitle(updatedFlight.toString() + " arrival terminal is now " + updatedFlight.getStatus().getArrivalTerminal());
                        notifBuilder.setContentText("Arrives at " + updatedFlight.getPrettyArrivalDate());
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_land);
                        break;
                    case ARRIVAL_TIME:
                        notifBuilder.setContentTitle(updatedFlight.toString() + " now arrives at " + updatedFlight.getPrettyArrivalDate());
//                        notifBuilder.setContentText("Arrives at " + updatedFlight.getPrettyArrivalDate());    TODO show delay difference?
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_land);
                        break;
                    case ARRIVAL_AIRPORT:
                        //TODO shouldn't happen, if it's diverted then it's also delayed and we only allow 1 change max here
                        notifBuilder.setContentTitle(updatedFlight.toString() + " now arrives at " + updatedFlight.getPrettyArrivalDate());
                        notifBuilder.setContentText("Arrives at " + updatedFlight.getPrettyArrivalDate());
                        notifBuilder.setSmallIcon(R.drawable.ic_diverted);
                        break;
                    case BAGGAGE_CLAIM:
                        notifBuilder.setContentTitle(updatedFlight.toString() + " baggage claim is now " + updatedFlight.getStatus().getBaggageClaim());
                        notifBuilder.setContentText("Arrives at " + updatedFlight.getPrettyArrivalDate());
                        notifBuilder.setSmallIcon(R.drawable.ic_baggage_black);
                        break;
                    case DEPARTURE_GATE:
                        notifBuilder.setContentTitle(updatedFlight.toString() + " departure gate is now " + updatedFlight.getStatus().getDepartureGate());
                        notifBuilder.setContentText("Takes off at " + updatedFlight.getPrettyDepartureDate());
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_takeoff);
                        break;
                    case DEPARTURE_TERMINAL:
                        notifBuilder.setContentTitle(updatedFlight.toString() + " departure terminal is now " + updatedFlight.getStatus().getDepartureTerminal());
                        notifBuilder.setContentText("Takes off at " + updatedFlight.getPrettyDepartureDate());
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_takeoff);
                        break;
                    case DEPARTURE_TIME:
                        notifBuilder.setContentTitle(updatedFlight.toString() + " now takes off at " + updatedFlight.getPrettyDepartureDate());
//                        notifBuilder.setContentText("Arrives at " + updatedFlight.getPrettyDepartureDate());  TODO show delay difference?
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_takeoff);
                        break;

                }
            }
        }

        //Build its action and set it to the notification
        Intent baseIntent = new Intent(NotificationService.this, FlightDetailMainActivity.class);
        baseIntent.putExtra(FlightDetailMainActivity.PARAM_FLIGHT, updatedFlight);
        //Set flags to take user back to Home when navigating back from details
//        baseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        //Build a pending intent with the recently constructed base intent and set it to the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, baseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notifBuilder.setContentIntent(pendingIntent);
        return notifBuilder.build();
    }

    /**
     * Sets the specified notification builder to prepare a notification for a flight whose status
     * changed. Will set the title to (flight) (newStatus) and the content as appropriate to the new
     * status.
     *
     * @param notifBuilder        The Notification builder building the notification.
     * @param flightWithNewStatus Flight whose status changed.
     */
    private void buildNotificationForSatatusChange(NotificationCompat.Builder notifBuilder, Flight flightWithNewStatus) {
        FlightStatus newStatus = flightWithNewStatus.getStatus();
        notifBuilder.setContentTitle(flightWithNewStatus.toString() + " " + newStatus.toString());
        switch (newStatus.getStatus()) {
            case "S":   //Scheduled
                notifBuilder.setSmallIcon(R.drawable.ic_scheduled);
                if (newStatus.getScheduledDepartureTime() != null) {
                    notifBuilder.setContentText(newStatus.getPrettyScheduledDepartureTime());
                }
                break;
            case "A":   //Active
                notifBuilder.setSmallIcon(R.drawable.ic_flight_takeoff);
                notifBuilder.setContentText("Took off at " + newStatus.getPrettyActualDepartureTime());
                break;
            case "D":   //Diverted
                notifBuilder.setSmallIcon(R.drawable.ic_diverted);
                notifBuilder.setContentText("Lands at " + newStatus.getDestinationAirport() + " at " + newStatus.getPrettyScheduledArrivalTime());  //TODO only notify new time if different from old time
                break;
            case "L":   //Landed
                notifBuilder.setSmallIcon(R.drawable.ic_flight_land);
                notifBuilder.setContentText("Landed at " + newStatus.getPrettyActualArrivalTime());
                break;
            case "C":   //Cancelled
                notifBuilder.setSmallIcon(R.drawable.ic_cancelled);
                notifBuilder.setContentText("=(");  //TODO wat say?
                break;
        }
    }
}
