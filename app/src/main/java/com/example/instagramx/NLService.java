//based on https://stackoverflow.com/questions/41425986/call-a-notification-listener-inside-a-background-service-in-android-studio

//convert chrome instagram notifications to instagram x notifications
package com.example.instagramx;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;


public class NLService extends NotificationListenerService {

    private String TAG = "InstagramXNotificationListener";
    int NotificationID = 0;

    public void createNotification(String NTitle, String NText) {
        Log.i(TAG, "Creating Notification");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.logo)
                        .setContentTitle(NTitle)
                        .setContentText(NText)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setChannelId("InstagramXNotificationChannel");

        //open app when clicked
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.instagramx");
        PendingIntent pIntent = PendingIntent.getActivity(this, 0,intent, FLAG_IMMUTABLE);
        mBuilder.setContentIntent(pIntent);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationID, mBuilder.build());
        NotificationID++;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "NLService startcommand!");

            super.onStartCommand(intent, flags, startId);

            // NOTE: We return STICKY to prevent the automatic service termination
            return START_STICKY;
        }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("InstagramXNotificationChannel", "InstagramX", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        Log.i(TAG, "NLService created!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "NLService destroyed!");
    }

    /* > API 21
    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.w(TAG, "Notification listener DISCONNECTED from the notification service! Scheduling a reconnect...");
        // requestRebind(new ComponentName(this.getPackageName(), this.getClass().getCanonicalName()));
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.w(TAG, "Notification listener connected with the notification service!");
    }
    */

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
            if(sbn.getNotification().tickerText == null || sbn.getPackageName() == null){
                return;
            }

            String tickerTextStr = sbn.getNotification().tickerText.toString();
            String packageName = sbn.getPackageName().toString();
            if (tickerTextStr.equals("Instagram\nYou have unseen notifications.")) {
                //remove annoying notification
                cancelNotification(sbn.getKey());

            } else if (packageName.equals("com.android.chrome") && tickerTextStr.startsWith("Instagram")) {
                //replace chrome instagram notification with instagram x notification
                Log.i(TAG, "**********  onNotificationPosted");
                Log.i(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

                //create new notification, removing first line
                createNotification("InstagramX", tickerTextStr.substring(tickerTextStr.indexOf('\n')+1));

                //remove original notification
                cancelNotification(sbn.getKey());
            } else if (packageName.equals("com.instagram.android")){
                //replace instagram notification with instagram x notification
                Log.i(TAG, "**********  onNotificationPosted");
                Log.i(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

                //create new notification, removing first line
                String Text = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString();
                String Title = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString();
                createNotification(Title, Text);

                //remove original notification
                cancelNotification(sbn.getKey());

            } else{
                Log.i(TAG, "**********  onNotificationPosted ignored");
                Log.i(TAG, packageName);
            }
    }

//    @Override
//    public void onNotificationRemoved(StatusBarNotification sbn) {
//        Log.i(TAG,"********** onNotificationRemoved");
//        Log.i(TAG,"ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
//    }


}