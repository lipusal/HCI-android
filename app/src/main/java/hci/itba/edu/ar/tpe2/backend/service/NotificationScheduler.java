package hci.itba.edu.ar.tpe2.backend.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import hci.itba.edu.ar.tpe2.R;

/**
 * Class used to set up the repeating alarm for the notification service.
 */
public class NotificationScheduler extends BroadcastReceiver {
    private static PendingIntent futureIntent;  //TODO I fear that if the app gets destroyed, this gets lost. Consider saving to file.

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {    //Set repeating alarm to check for flight updates on device boot
            long frequency = PreferenceManager.getDefaultSharedPreferences(context).getLong(context.getString(R.string.pref_key_update_frequency), -1);
            setUpdates(context, frequency);
        }
    }

    public static void setUpdates(Context context, long frequency) {
        if (frequency != -1) {
            cancelUpdates(context);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent baseIntent = new Intent(context, NotificationService.class);
            baseIntent.setAction(NotificationService.ACTION_NOTIFY_UPDATES);
            futureIntent = PendingIntent.getService(context, 0, baseIntent, 0);
            manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, frequency, futureIntent);
            Log.d("VOLANDO", "Set flight status update frequency to " + frequency);
        }
        else {
            Log.d("VOLANDO", "Flight status update frequency is -1, ignoring. If you wanted to cancel updates, call cancelUpdates()");
        }
    }

    public static void cancelUpdates(Context context) {
        if (areUpdatesEnabled()) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.cancel(futureIntent);
            futureIntent = null;
        }
    }

    public static boolean areUpdatesEnabled() {
        return futureIntent != null;
    }
}