package hci.itba.edu.ar.tpe2.backend.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
 *      intent.putExtra(PARAM_FLIGHT, "AA");
 *      intent.putExtra(PARAM_FLIGHT_NUM, 1234);
 *      this.startService(intent);
 * }
 * </pre>
 */
public class NotificationService extends IntentService {
    public static final String ACTION_NOTIFY_UPDATES = "hci.itba.edu.ar.tpe2.backend.service.action.NOTIFY_UPDATES",
            PARAM_FLIGHT = "hci.itba.edu.ar.tpe2.backend.service.extra.FLIGHT";

    public NotificationService() { super("NotificationService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.getAction().equals(ACTION_NOTIFY_UPDATES) && intent.hasExtra(PARAM_FLIGHT)) {
            Flight flight = (Flight) intent.getSerializableExtra(PARAM_FLIGHT);
            notifyUpdates(flight);
        }
    }

    private void notifyUpdates(final Flight flight) {
        Bundle params = new Bundle();
        params.putString("airline_id", flight.getAirlineID());
        params.putString("flight_number", Integer.toString(flight.getNumber()));
        new APIRequest(API.Service.status, params) {
            @Override
            protected void successCallback(String result) {
                Gson g = new Gson();
                JsonObject json = g.fromJson(result, JsonObject.class);
                FlightStatus newStatus = g.fromJson(json.get("status"), FlightStatus.class);
                if (!newStatus.equals(flight.getStatus())) {
                    //TODO send notification here
                    //It would be nice if notifications stacked if there is more than 1 flight change
                }
                //TODO schedule a future Intent to check for updates again
                //(hardcode something like 5 min for now, later use settings)
            }

            @Override
            protected void errorCallback(String result) {
                super.errorCallback("Error getting status updates for " + flight.getAirlineID() + " #" + flight.getNumber() + ":\n" + result);
            }
        };
    }
}
