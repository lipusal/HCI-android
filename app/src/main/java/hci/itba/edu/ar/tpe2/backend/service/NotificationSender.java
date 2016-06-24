package hci.itba.edu.ar.tpe2.backend.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import hci.itba.edu.ar.tpe2.FlightDetailMainActivity;
import hci.itba.edu.ar.tpe2.FlightsActivity;
import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatusComparator;

/**
 * Receiver used to send notifications to the user when outside the app. Updates statuses in
 * persistent data. Receives broadcasts from {@link UpdateService} only if no other activity caught
 * the broadcast first.
 */
public class NotificationSender extends BroadcastReceiver {
    private static final String GROUP_NOTIFICATION_KEY = "hci.itba.edu.ar.tpe2.backend.service.NotificationSender.GROUP_NOTIFICATION_KEY";
    private static final int GROUP_NOTIFICATION_ID = 42;

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

        //Build group notification or single notification
        Notification notif = null;
        int notifID = -1;
        for (Map.Entry<Integer, FlightStatus> entry : updatedStatuses.entrySet()) { //Will only run once (at most)
            if (updatedStatuses.size() == 1) {
                notif = buildSingleNotification(
                        entry.getValue(),
                        differences.get(entry.getKey()),
                        context
                );
                notifID = entry.getKey();
            } else {    //Size > 1
                notif = buildGroupNotification(updatedStatuses.values(), context);
                notifID = GROUP_NOTIFICATION_ID;
                break;
            }
        }
        //If built, send
        if (notif != null) {
            NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.notify(notifID, notif);
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
    private Notification buildSingleNotification(FlightStatus updatedStatus, Map<FlightStatusComparator.ComparableField, Serializable> differences, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //Build the base notification
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)                                                //Dismiss notification when clicking
                .setCategory(Notification.CATEGORY_STATUS)
                .setPriority(Notification.PRIORITY_HIGH)                            //Trigger heads-up
                .setSound(Uri.parse(preferences.getString(context.getString(R.string.pref_key_notification_ringtone), context.getString(R.string.pref_default_ringtone))))
                .setGroup(GROUP_NOTIFICATION_KEY)
                .setSmallIcon(updatedStatus.getIconID())
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), updatedStatus.getIconID()));

        //Vibrate?
        if (preferences.getBoolean(context.getString(R.string.pref_key_vibrate_on_notify), Boolean.parseBoolean(context.getString(R.string.pref_default_vibrate_on_notify)))) {
            notifBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        //Text content
        if (differences.size() > 1) {
            notifBuilder.setContentTitle(String.format(context.getString(R.string.status_changed_generic), updatedStatus.getFlight().toString()));
            notifBuilder.setContentText(String.format(context.getString(R.string.tap_to_see_details)));
        } else {
            for (Map.Entry<FlightStatusComparator.ComparableField, Serializable> difference : differences.entrySet()) {    //Should only run once, but it's the only way to iterate over a set
                switch (difference.getKey()) {
                    case STATUS:
                        buildNotificationForSatatusChange(notifBuilder, updatedStatus, context);
                        break;
                    case DEPARTURE_GATE:
                        notifBuilder.setContentTitle(String.format(context.getString(R.string.departure_gate_changed), updatedStatus.getFlight().toString(), updatedStatus.getDepartureGate()));
                        notifBuilder.setContentText(String.format(context.getString(R.string.departs_at), updatedStatus.getPrettyScheduledDepartureTime()));
                        break;
                    case DEPARTURE_TERMINAL:
                        notifBuilder.setContentTitle(String.format(context.getString(R.string.departure_terminal_changed), updatedStatus.getFlight().toString(), updatedStatus.getDepartureTerminal()));
                        notifBuilder.setContentText(String.format(context.getString(R.string.departs_at), updatedStatus.getPrettyScheduledDepartureTime()));
                        break;
                    case DEPARTURE_TIME:
                        notifBuilder.setContentTitle(String.format(context.getString(R.string.departs_at), updatedStatus.getPrettyScheduledDepartureTime()));
                        break;
                    case ARRIVAL_GATE:
                        notifBuilder.setContentTitle(String.format(context.getString(R.string.arrival_gate_changed), updatedStatus.getFlight().toString(), updatedStatus.getArrivalGate()));
                        notifBuilder.setContentText(String.format(context.getString(R.string.arrives_at), updatedStatus.getPrettyScheduledArrivalTime()));
                        break;
                    case ARRIVAL_TERMINAL:
                        notifBuilder.setContentTitle(String.format(context.getString(R.string.arrival_terminal_changed), updatedStatus.getFlight().toString(), updatedStatus.getArrivalTerminal()));
                        notifBuilder.setContentText(String.format(context.getString(R.string.arrives_at), updatedStatus.getPrettyScheduledArrivalTime()));
                        break;
                    case ARRIVAL_TIME:
                        notifBuilder.setContentTitle(String.format(context.getString(R.string.arrives_at), updatedStatus.getPrettyScheduledArrivalTime()));
                        break;
                    case ARRIVAL_AIRPORT:
                        notifBuilder.setContentTitle(String.format(context.getString(R.string.diverted_to), updatedStatus.getDestinationAirport().toString()));
                        notifBuilder.setContentText(String.format(context.getString(R.string.arrives_at), updatedStatus.getPrettyScheduledArrivalTime()));
                        break;
                    case BAGGAGE_CLAIM:
                        notifBuilder.setContentTitle(String.format(context.getString(R.string.baggage_claim_changed), updatedStatus.getFlight().toString(), updatedStatus.getBaggageClaim()));
                        notifBuilder.setContentText(String.format(context.getString(R.string.arrives_at), updatedStatus.getPrettyScheduledArrivalTime()));
                        notifBuilder.setSmallIcon(R.drawable.ic_baggage_black);
                        notifBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baggage_black));
                        break;
                }
            }
        }

        //Set action to Details activity, with back stack to Flights
        Intent homeIntent = new Intent(context, FlightsActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Intent detailsIntent = new Intent(context, FlightDetailMainActivity.class);
        detailsIntent.putExtra(FlightDetailMainActivity.PARAM_STATUS, updatedStatus);
        PendingIntent pendingIntentWithBackStack = TaskStackBuilder
                .create(context)
                .addNextIntent(homeIntent)
                .addNextIntent(detailsIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notifBuilder.setContentIntent(pendingIntentWithBackStack);
        return notifBuilder.build();
    }

    /**
     * Sets the specified notification builder to prepare a notification for a flight whose status
     * changed. Will set the title to (flight) (newStatus) and the content as appropriate to the new
     * status.
     *  @param notifBuilder The Notification builder building the notification.
     * @param newStatus    The updated status.
     * @param context
     */
    private void buildNotificationForSatatusChange(NotificationCompat.Builder notifBuilder, FlightStatus newStatus, Context context) {
        notifBuilder.setContentTitle(newStatus.getFlight().toString() + " " + context.getString(newStatus.getStringResID()));
        switch (newStatus.getStatus()) {
            case "S":   //Scheduled
                notifBuilder.setContentText(String.format(context.getString(R.string.departs_at), newStatus.getPrettyScheduledDepartureTime()));
                break;
            case "A":   //Active
                notifBuilder.setContentText(String.format(context.getString(R.string.departed_at), newStatus.getPrettyActualDepartureTime()));
                break;
            case "R":   //Diverted
                notifBuilder.setContentText(String.format(context.getString(R.string.diverted_to), newStatus.getDestinationAirport().getName()));
                break;
            case "L":   //Landed
                notifBuilder.setContentText(String.format(context.getString(R.string.arrived_at), newStatus.getPrettyActualArrivalTime()));
                break;
            case "C":   //Cancelled
//                notifBuilder.setContentText("=(");  //TODO wat say?
                break;
        }
    }

    private Notification buildGroupNotification(Collection<FlightStatus> updatedStatuses, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(String.format(context.getString(R.string.x_flights_updated), updatedStatuses.size()))
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_STATUS)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(Uri.parse(preferences.getString(context.getString(R.string.pref_key_notification_ringtone), context.getString(R.string.pref_default_ringtone))))
                .setSmallIcon(R.drawable.ic_flight)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_flight))
                .setNumber(updatedStatuses.size())
                .setGroup(GROUP_NOTIFICATION_KEY)
                .setGroupSummary(true);

        //Vibration
        if (preferences.getBoolean(context.getString(R.string.pref_key_vibrate_on_notify), Boolean.parseBoolean(context.getString(R.string.pref_default_vibrate_on_notify)))) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        //Summarize
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                .setBigContentTitle(String.format(context.getString(R.string.x_flights_updated), updatedStatuses.size()))
                .setSummaryText(context.getString(R.string.app_name));
        StringBuilder subtitleBuilder = new StringBuilder();
        for (FlightStatus status : updatedStatuses) {
            style.addLine(status.getFlight().toString() + "   " + context.getString(status.getStringResID()));
            subtitleBuilder.append(status.getFlight().toString() + ", ");
        }
        builder.setStyle(style);
        subtitleBuilder.setLength(subtitleBuilder.length() - 2);
        builder.setContentText(subtitleBuilder.toString());

        //Set action to Flights activity, no back stack
        Intent baseIntent = new Intent(context, FlightsActivity.class);
        baseIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, baseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        //Done, build
        return builder.build();
    }
}
