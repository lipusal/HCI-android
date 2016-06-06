package hci.itba.edu.ar.tpe2.backend.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import com.google.gson.Gson;

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
 *      intent.putExtra(PARAM_AIRLINE_ID, "AA");
 *      intent.putExtra(PARAM_FLIGHT_NUM, 1234);
 *      this.startService(intent);
 * }
 * </pre>
 */
public class NotificationService extends IntentService {
    public static final String ACTION_NOTIFY_UPDATES = "hci.itba.edu.ar.tpe2.backend.service.action.NOTIFY_UPDATES",
                                PARAM_AIRLINE_ID = "hci.itba.edu.ar.tpe2.backend.service.extra.AIRLINE_ID",
                                PARAM_FLIGHT_NUM = "hci.itba.edu.ar.tpe2.backend.service.extra.FLIGHT_NUM",
                                PARAM_PREV_STATUS = "hci.itba.edu.ar.tpe2.backend.service.extra.PREV_STATUS";

    public NotificationService() { super("NotificationService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.getAction().equals(ACTION_NOTIFY_UPDATES)) {
            String airlineID = intent.getStringExtra(PARAM_AIRLINE_ID),
                    prevStatus = intent.getStringExtra(PARAM_PREV_STATUS);
            int flightNum = intent.getIntExtra(PARAM_FLIGHT_NUM, -1);
            if(airlineID != null && prevStatus != null && flightNum != -1) {
                notifyUpdates(airlineID, flightNum, prevStatus);
            }
        }
    }

    private void notifyUpdates(final String airlineID, final int flightNum, String previousStatus) {
        Bundle params = new Bundle();
        params.putString("airline_id", airlineID);
        params.putString("flight_number", Integer.toString(flightNum));
        new APIRequest(API.Service.status, params) {
            @Override
            protected void successCallback(String result) {
                Gson g = new Gson();
                //TODO Parse JSON, if flight status has changed since last time, notify, else do nothing
                //TODO it would be nice if notifications stacked if there is more than 1 flight change
            }

            @Override
            protected void errorCallback(String result) {
                super.errorCallback("Error getting status updates for " + airlineID + " #" + flightNum + ":\n" + result);
            }
        };
    }
}
