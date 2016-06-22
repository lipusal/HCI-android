package hci.itba.edu.ar.tpe2.backend.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import hci.itba.edu.ar.tpe2.FlightDetailMainActivity;
import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatusComparator;

/**
 * Receiver used to send notifications to the user when outside the app. Updates statuses in
 * persistent data. Receives broadcasts from {@link UpdateService} only if no other activity caught
 * the broadcast first.
 */
public class NotificationSender extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean sendNotifications = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_notify_on_update), Boolean.parseBoolean(context.getString(R.string.pref_default_notify_on_update)));
        if (!sendNotifications) {
            return;
        }
        Map<Integer, FlightStatus> updatedStatuses = (Map<Integer, FlightStatus>) intent.getSerializableExtra(UpdateService.EXTRA_UPDATES);
        Map<Integer, Map<FlightStatusComparator.ComparableField, Serializable>> differences = (Map<Integer, Map<FlightStatusComparator.ComparableField, Serializable>>) intent.getSerializableExtra(UpdateService.EXTRA_DIFFERENCES);
        if (updatedStatuses.isEmpty()) {
            return;
        }

        /**
         * Calculate differences and build notifications as appropriate
         */
        final Map<Integer, Notification> notifications = new HashMap<>(updatedStatuses.size());
        for (Map.Entry<Integer, FlightStatus> entry : updatedStatuses.entrySet()) {
            int id = entry.getKey();
            notifications.put(
                    id,
                    buildNotification(
                            entry.getValue(),
                            differences.get(id),
                            context
                    )
            );
        }
        //Notifications built, send all of them
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        for (Map.Entry<Integer, Notification> entry : notifications.entrySet()) {
            notifManager.notify(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Builds a notification with the appropriate information for the new status of the specified
     * flight. Will notify about the fields present in {@code differences}.
     *
     * @param updatedStatus The updated status.
     * @param differences   A map with the fields of the updated fields, obtained via a
     *                      {@link FlightStatusComparator}. Should not be empty.
     * @param context       Context to build notifications.
     * @return An appropriate notification.
     */
    private Notification buildNotification(FlightStatus updatedStatus, Map<FlightStatusComparator.ComparableField, Serializable> differences, Context context) {
        //TODO provide big icon (i.e. for Moto X active display)
        //TODO group notifications for various updates of the same flight
        //TODO if the user so desires, group all notifications of the app into one
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //Build the base notification
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)                                                //Dismiss notification when clicking
                .setCategory(Notification.CATEGORY_STATUS)
                .setPriority(Notification.PRIORITY_HIGH)                            //Trigger heads-up
                //This mouthful gets the sound as set in settings, and falls back to default notification sound if not found TODO not working, plays no sound
                .setSound(Uri.parse(preferences.getString(context.getString(R.string.pref_key_notification_ringtone), context.getString(R.string.pref_default_ringtone))));
        //Set extra parameters
        if (preferences.getBoolean(context.getString(R.string.pref_key_vibrate_on_notify), Boolean.parseBoolean(context.getString(R.string.pref_default_vibrate_on_notify)))) {
            notifBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        //Build content
        if (differences.size() > 1) {
            Log.d("VOLANDO", "Multiple differences for " + updatedStatus.getFlight().toString() + ": " + differences.entrySet().toString());
            notifBuilder.setContentTitle(updatedStatus.getFlight().toString() + " status changed");
            notifBuilder.setContentText("Tap to see all changes");
            notifBuilder.setSmallIcon(R.drawable.ic_flight);
        } else {
            for (Map.Entry<FlightStatusComparator.ComparableField, Serializable> difference : differences.entrySet()) {    //Should only run once, but it's the only way to iterate over a set
                switch (difference.getKey()) {
                    case STATUS:
                        buildNotificationForSatatusChange(notifBuilder, updatedStatus);
                        break;
                    case ARRIVAL_GATE:
                        notifBuilder.setContentTitle(updatedStatus.getFlight().toString() + " arrival gate is now " + updatedStatus.getArrivalGate());    //TODO use string resources for this
                        notifBuilder.setContentText("Arrives at " + updatedStatus.getPrettyScheduledArrivalTime());
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_land);
                        break;
                    case ARRIVAL_TERMINAL:
                        notifBuilder.setContentTitle(updatedStatus.getFlight().toString() + " arrival terminal is now " + updatedStatus.getArrivalTerminal());
                        notifBuilder.setContentText("Arrives at " + updatedStatus.getPrettyScheduledArrivalTime());
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_land);
                        break;
                    case ARRIVAL_TIME:
                        notifBuilder.setContentTitle(updatedStatus.getFlight().toString() + " now arrives at " + updatedStatus.getPrettyScheduledArrivalTime());
//                        notifBuilder.setContentText("Arrives at " + updatedFlight.getPrettyArrivalDate());    TODO show delay difference?
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_land);
                        break;
                    case ARRIVAL_AIRPORT:
                        //TODO shouldn't happen, if it's diverted then it's also delayed and we only allow 1 change max here
                        notifBuilder.setContentTitle(updatedStatus.getFlight().toString() + " now arrives at " + updatedStatus.getDestinationAirport().toString());
                        notifBuilder.setContentText("Arrives at " + updatedStatus.getPrettyScheduledArrivalTime());
                        notifBuilder.setSmallIcon(R.drawable.ic_diverted);
                        break;
                    case BAGGAGE_CLAIM:
                        notifBuilder.setContentTitle(updatedStatus.getFlight().toString() + " baggage claim is now " + updatedStatus.getBaggageClaim());
                        notifBuilder.setContentText("Arrives at " + updatedStatus.getPrettyScheduledArrivalTime());
                        notifBuilder.setSmallIcon(R.drawable.ic_baggage_black);
                        break;
                    case DEPARTURE_GATE:
                        notifBuilder.setContentTitle(updatedStatus.getFlight().toString() + " departure gate is now " + updatedStatus.getDepartureGate());
                        notifBuilder.setContentText("Takes off at " + updatedStatus.getPrettyScheduledDepartureTime());
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_takeoff);
                        break;
                    case DEPARTURE_TERMINAL:
                        notifBuilder.setContentTitle(updatedStatus.getFlight().toString() + " departure terminal is now " + updatedStatus.getDepartureTerminal());
                        notifBuilder.setContentText("Takes off at " + updatedStatus.getPrettyScheduledDepartureTime());
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_takeoff);
                        break;
                    case DEPARTURE_TIME:
                        notifBuilder.setContentTitle(updatedStatus.getFlight().toString() + " now takes off at " + updatedStatus.getPrettyScheduledDepartureTime());
//                        notifBuilder.setContentText("Arrives at " + updatedFlight.getPrettyDepartureDate());  TODO show delay difference?
                        notifBuilder.setSmallIcon(R.drawable.ic_flight_takeoff);
                        break;

                }
            }
        }

        //Build its action and set it to the notification
        Intent baseIntent = new Intent(context, FlightDetailMainActivity.class);
        baseIntent.putExtra(FlightDetailMainActivity.PARAM_STATUS, updatedStatus);
        //Set flags to take user back to Home when navigating back from details
        //TODO flags here
//        baseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        //Build a pending intent with the recently constructed base intent and set it to the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, baseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notifBuilder.setContentIntent(pendingIntent);
        return notifBuilder.build();
    }

    /**
     * Sets the specified notification builder to prepare a notification for a flight whose status
     * changed. Will set the title to (flight) (newStatus) and the content as appropriate to the new
     * status.
     *
     * @param notifBuilder The Notification builder building the notification.
     * @param newStatus    The updated status.
     */
    private void buildNotificationForSatatusChange(NotificationCompat.Builder notifBuilder, FlightStatus newStatus) {
        notifBuilder.setContentTitle(newStatus.getFlight().toString() + " " + newStatus.toString());
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
