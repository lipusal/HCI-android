package hci.itba.edu.ar.tpe2.backend.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import hci.itba.edu.ar.tpe2.FlightDetailsActivity;
import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.FileManager;
import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.network.API;
import hci.itba.edu.ar.tpe2.backend.network.APIRequest;

/**
 * Service used for checking for flight updates in the background. Will notify the user if there has
 * been a change in the specified flight's status.
 * <br /><br />
 * To check for updates for flight AA 1234, do the following (assuming {@code this} is a
 * subclass of {@link android.content.Context Context}):
 * <pre>
 * {@code
 *   Intent intent = new Intent(this, NotificationService.class);
 *      intent.setAction(ACTION_NOTIFY_UPDATES);
 *      intent.putExtra(PARAM_FLIGHTS_LIST, "AA");
 *      intent.putExtra(PARAM_FLIGHT_NUM, 1234);
 *      this.startService(intent);
 * }
 * </pre>
 */
public class NotificationService extends IntentService {
    public static final String ACTION_NOTIFY_UPDATES = "hci.itba.edu.ar.tpe2.backend.service.action.NOTIFY_UPDATES";

    public NotificationService() { super("NotificationService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            //TODO ensure repeating alarm is sending the intent properly (see NotificationScheduler)
            switch(intent.getAction()) {
                case ACTION_NOTIFY_UPDATES:
                    notifyUpdates();
                    break;
            }
        }
    }

    /**
     * Checks for changes in the user's followed flights, and sends notifications for any flights
     * whose status changed.
     */
    private void notifyUpdates() {
        final FileManager fileManager = new FileManager(this);
        final List<Flight> flights = fileManager.loadFollowedFlights();
        if(flights.size() > 0) {
            Log.d("VOLANDO", "Fetching updates for " + flights.size() + " followed flights");
        }
        else {
            Log.d("VOLANDO", "No followed flights, not checking updates");
            return;
        }
        final Gson g = new Gson();
        final AtomicInteger requestsLeft = new AtomicInteger(flights.size());       //To avoid race condition when waiting for all AsyncTasks to complete
        final Map<Integer, Notification> notifications = new HashMap<>(flights.size());
        final Boolean[] changed = {false};
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Make an async network request for each flight that needs updates. Send all notifications at
        //the same time once ALL updates have been completed.
        for(final Flight flight : flights) {
            Bundle params = new Bundle();
            params.putString("method", API.Method.getflightstatus.name());
            params.putString("airline_id", flight.getAirline().getID());
            params.putString("flight_number", Integer.toString(flight.getNumber()));
            new APIRequest(API.Service.status, params) {
                @Override
                protected void successCallback(String result) {
                    JsonObject json = g.fromJson(result, JsonObject.class);
                    FlightStatus newStatus = g.fromJson(json.get("status"), FlightStatus.class);
                    if (!newStatus.equals(flight.getStatus())) {
                        Log.d("VOLANDO", "Status changed to " + newStatus.toString() + " for " + flight.toString());
                        flight.setStatus(newStatus);
                        //Build the base notification
                        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(NotificationService.this)
                                .setSmallIcon(R.drawable.ic_star_on_24dp)
                                .setContentTitle(flight.toString() + " " + newStatus.toString())
                                .setContentText("This is not done yet")
                                .setAutoCancel(true)                                                //Dismiss notification when clicking
                                .setCategory(Notification.CATEGORY_STATUS)
                                .setPriority(Notification.PRIORITY_HIGH)                            //Trigger heads-up
                                .setTicker(flight.toString() + " " + newStatus.toString())          //Text to display when the notif first arrives
                                //This mouthful gets the sound as set in settings, and falls back to default notification sound if not found TODO not working, plays no sound
                                .setSound(Uri.parse(preferences.getString(NotificationService.this.getString(R.string.pref_key_update_frequency), NotificationService.this.getString(R.string.pref_default_ringtone))));
                        if (preferences.getBoolean(NotificationService.this.getString(R.string.pref_key_vibrate_on_notify), false)) {
                            notifBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                        }
                        //Build its action and set it to the notification
                        Intent baseIntent = new Intent(NotificationService.this, FlightDetailsActivity.class);
                        baseIntent.putExtra(FlightDetailsActivity.PARAM_FLIGHT, flight);
                        //Set flags to take user back to Home when navigating back from details TODO this makes the application close when hitting back, user should be taken to home
//                        baseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                        //Build a pending intent with the recently constructed base intent and set it to the notification
                        PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, baseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        notifBuilder.setContentIntent(pendingIntent);
                        //Add it to the map, notifications will be sent together
                        notifications.put(flight.getID(), notifBuilder.build());
                        changed[0] = true;
                    }
                    if(requestsLeft.decrementAndGet() == 0) {   //Avoids race condition
                        if (changed[0]) {
                            //First, save the updated flights
                            fileManager.saveFollowedFlights(flights);
                            //All updates completed, send all notifications at the same time
                            NotificationManager notifManager = (NotificationManager) NotificationService.this.getSystemService(NOTIFICATION_SERVICE);
                            for (Map.Entry<Integer, Notification> entry : notifications.entrySet()) {
                                notifManager.notify(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }

                @Override
                protected void errorCallback(String result) {
                    super.errorCallback("Error getting status updates for " + flight.toString() + ":\n" + result);
                }
            }.execute();
        }
    }
}
