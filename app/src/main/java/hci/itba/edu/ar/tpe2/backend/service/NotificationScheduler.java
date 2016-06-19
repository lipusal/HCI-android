package hci.itba.edu.ar.tpe2.backend.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
        switch (intent.getAction()) {
            case "android.intent.action.BOOT_COMPLETED":    //Set repeating alarm to check for flight updates on device boot
                long frequency = PreferenceManager.getDefaultSharedPreferences(context).getLong(context.getString(R.string.pref_key_update_frequency), -1);
                setUpdateFrequency(context, frequency);
                break;
            case "android.net.conn.CONNECTIVITY_CHANGE":    //TODO My Moto X receives this broadcast more than once, see http://stackoverflow.com/a/8413512/2333689
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (!isConnected) {  //Recently disconnected, don't bother fetching updates with no connection
                    Log.d("VOLANDO", "Detected network disconnect, cancelling automatic updates");
                    cancelUpdates(context);
                } else {  //Recently (re-)connected
                    if (!areUpdatesEnabled()) {
                        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                        if (isWiFi) {
                            Log.d("VOLANDO", "Re/connected to WiFi, setting automatic updates");
                            setDefaultUpdateFrequency(context);
                        } else {  //Mobile data, enable updates only if set in preferences
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                            boolean updateOverMobileData = preferences.getBoolean(context.getString(R.string.pref_key_update_on_cellular_network), true);
                            if (updateOverMobileData) {
                                Log.d("VOLANDO", "Re/connected to mobile data and updates enabled over mobile data, setting automatic updates");
                                setDefaultUpdateFrequency(context);
                            } else {
                                Log.d("VOLANDO", "Re/connected to mobile data but updates are not allowed over mobile data, won't set updates");
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * Sets update frequency to the specified millisecond interval.
     *
     * @param context   Application context for getting the alarm system service.
     * @param frequency New frequency in milliseconds, will ignore if -1 is specified.
     */
    public static void setUpdateFrequency(Context context, long frequency) {
        if (frequency != -1) {
            cancelUpdates(context);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent baseIntent = new Intent(context, NotificationService.class);
            baseIntent.setAction(NotificationService.ACTION_NOTIFY_UPDATES);
            futureIntent = PendingIntent.getService(context, 0, baseIntent, 0);
            manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, frequency, futureIntent);
            Log.d("VOLANDO", "Set flight status update frequency to " + frequency);
        } else {
            Log.d("VOLANDO", "Flight status update frequency is -1, ignoring. If you wanted to cancel updates, call cancelUpdates()");
        }
    }

    /**
     * Sets update frequency to that set in preferences.
     *
     * @param context Application context for getting preferences and alarm service.
     * @see #setUpdateFrequency(Context, long)
     */
    public static void setDefaultUpdateFrequency(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long freq = Long.parseLong(preferences.getString(context.getString(R.string.pref_key_update_frequency), "-1"));

        setUpdateFrequency(context, freq);
    }

    /**
     * Cancels automatic updates.
     *
     * @param context Application context for getting the alarm sevice.
     */
    public static void cancelUpdates(Context context) {
        if (areUpdatesEnabled()) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.cancel(futureIntent);
            futureIntent = null;
        }
    }

    /**
     * @return Whether automatic updates are enabled.
     */
    public static boolean areUpdatesEnabled() {
        return futureIntent != null;
    }
}