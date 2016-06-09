package hci.itba.edu.ar.tpe2.backend.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Class used to set up the repeating alarm for the notification service.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //Set repeating alarm to check for flight updates on device boot
            long frequency = 5 * 60 * 1000;     //5 minutes TODO get it from settings
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent baseIntent = new Intent(context, NotificationService.class);
            baseIntent.setAction(NotificationService.ACTION_NOTIFY_UPDATES);
            PendingIntent futureIntent = PendingIntent.getBroadcast(context, 0, baseIntent, 0);
            manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, frequency, futureIntent);
        }
    }
}