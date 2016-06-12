package hci.itba.edu.ar.tpe2.backend.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
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
                    List<Flight> flights = PersistentData.getInstance().getFollowedFlights();
                    notifyUpdates(flights);
                    break;
            }
        }
    }

    /**
     * Checks for changes in the status of the specified flights, and sends notifications for any
     * flights whose status changed.
     *
     * @param flights The flights for which to check for status changes.
     */
    private void notifyUpdates(final List<Flight> flights) {
        final Gson g = new Gson();
        final AtomicInteger requestsLeft = new AtomicInteger(flights.size());       //To avoid race condition when waiting for all AsyncTasks to complete
        if(flights.size() > 0) {
            Log.d("VOLANDO", "Fetching updates for " + flights.size() + " followed flights");
        }
        else {
            Log.d("VOLANDO", "No followed flights, not checking updates");
        }
        //TODO make collection of notifications here. If notifications are needed, they will be added as requests complete
        //Make an async network request for each flight that needs updates. Send all notifications at
        //the same time once ALL updates have been completed.
        for(final Flight flight : flights) {
            Bundle params = new Bundle();
            params.putString("airline_id", flight.getAirline().getID());
            params.putString("flight_number", Integer.toString(flight.getNumber()));
            new APIRequest(API.Service.status, params) {
                @Override
                protected void successCallback(String result) {
                    JsonObject json = g.fromJson(result, JsonObject.class);
                    FlightStatus newStatus = g.fromJson(json.get("status"), FlightStatus.class);
                    if (!newStatus.equals(flight.getStatus())) {
                        //TODO make a new notification BUT DON'T SEND IT. Add it to the collection (see previous TODO)
                    }
                    if(requestsLeft.decrementAndGet() == 0) {   //Avoids race condition
                        //TODO all updates completed, send all notifications here at the same time
                    }
                }

                @Override
                protected void errorCallback(String result) {
                    super.errorCallback("Error getting status updates for " + flight.getAirline().getID() + " #" + flight.getNumber() + ":\n" + result);
                }
            };
        }
    }
}
