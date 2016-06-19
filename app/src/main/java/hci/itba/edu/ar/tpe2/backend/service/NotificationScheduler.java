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
    public static final String ACTION_UPDATE_OVER_NETWORK_SETTING_CHANGED = "hci.itba.edu.ar.tpe2.backend.service.action.UPDATE_OVER_NETWORK_SETTING_CHANGED",
            ACTION_UPDATE_FREQUENCY_SETTING_CHANGED = "hci.itba.edu.ar.tpe2.backend.service.action.UPDATE_FREQUENCY_SETTING_CHANGED";
    private static PendingIntent futureIntent;


    /**
     * @return Whether automatic updates are enabled.
     */
    public static boolean areUpdatesEnabled() {
        return futureIntent != null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        switch (intent.getAction()) {
            case "android.intent.action.BOOT_COMPLETED":
            case ACTION_UPDATE_FREQUENCY_SETTING_CHANGED:
                setDefaultUpdateFrequency(context);
                break;
            case "android.net.conn.CONNECTIVITY_CHANGE":    //TODO My Moto X receives this broadcast more than once, see http://stackoverflow.com/a/8413512/2333689
                if (!isConnected(context)) {  //Recently disconnected, don't bother fetching updates with no connection
                    Log.d("VOLANDO", "Detected network disconnect, disabling automatic updates");
                    disableUpdates(context);
                } else {  //Recently (re-)connected
                    if (!areUpdatesEnabled()) {
                        if (isConnectedToWiFi(context)) {
                            Log.d("VOLANDO", "Re/connected to WiFi, setting automatic updates");
                            setDefaultUpdateFrequency(context);
                        } else {  //Mobile data, enable updates only if set in preferences
                            boolean updateOverMobileData = preferences.getBoolean(context.getString(R.string.pref_key_update_on_cellular_network), true);
                            if (updateOverMobileData) {
                                Log.d("VOLANDO", "Re/connected to mobile data and updates enabled over mobile data, setting automatic updates");
                                setDefaultUpdateFrequency(context);
                            } else {
                                Log.d("VOLANDO", "Re/connected to mobile data but updates are not allowed over mobile data, won't set updates");
                                //no need to call disableUpdates(), updates are disabled
                            }
                        }
                    } else {
                        Log.d("VOLANDO", "Re/connected but updates already set, not doing anything.");
                    }
                }
                break;
            case ACTION_UPDATE_OVER_NETWORK_SETTING_CHANGED:
                if (isConnected(context) && !isConnectedToWiFi(context)) {
                    boolean updateOverMobileData = preferences.getBoolean(context.getString(R.string.pref_key_update_on_cellular_network), true);
                    if (updateOverMobileData) {
                        Log.d("VOLANDO", "Enabled mobile data updates and connected to mobiile network, setting automatic updates");
                        setDefaultUpdateFrequency(context);
                    } else {
                        Log.d("VOLANDO", "Disabled mobile data updates while on mobile network, disabling updates");
                        disableUpdates(context);
                    }
                } else {
                    Log.d("VOLANDO", "Ignoring mobile network setting change, will take effect when connecting to mobile network");
                }
                break;
        }
    }

    /**
     * Sets update frequency to that set in preferences.
     *
     * @param context Application context for getting preferences and alarm service.
     * @see #setUpdateFrequency(Context, long)
     */
    private static void setDefaultUpdateFrequency(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long freq = Long.parseLong(preferences.getString(context.getString(R.string.pref_key_update_frequency), "-1"));
        setUpdateFrequency(context, freq);
    }

    /**
     * Sets update frequency to the specified millisecond interval.
     *
     * @param context   Application context for getting the alarm system service.
     * @param frequency New frequency in milliseconds, will ignore if -1 is specified.
     */
    private static void setUpdateFrequency(Context context, long frequency) {
        disableUpdates(context);
        if (frequency != -1) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent baseIntent = new Intent(context, NotificationService.class);
            baseIntent.setAction(NotificationService.ACTION_NOTIFY_UPDATES);
            futureIntent = PendingIntent.getService(context, 0, baseIntent, 0);
            manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, frequency, futureIntent);
            Log.d("VOLANDO", "Set automatic update frequency to " + frequency + "ms");
        } else {
            Log.d("VOLANDO", "Disabled automatic updates");
        }
    }

    /**
     * Cancels automatic updates.
     *
     * @param context Application context for getting the alarm sevice.
     */
    private static void disableUpdates(Context context) {
        if (areUpdatesEnabled()) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.cancel(futureIntent);
            futureIntent = null;
        }
    }

    /**
     * Checks whether the device is connected to the Internet.
     *
     * @param context Context through which to get the connectivity service.
     * @return Whether the device is connected to the Internet.
     */
    private static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    /**
     * Checks whether the device is connected to the Internet via WiFi.
     *
     * @param context Context through which to get the connectivity service.
     * @return Whether the device is connected to the Internet via WiFi.
     */
    private static boolean isConnectedToWiFi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }
}