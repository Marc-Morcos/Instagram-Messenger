//based on https://stackoverflow.com/questions/41425986/call-a-notification-listener-inside-a-background-service-in-android-studio

//convert chrome instagram notifications to instagram Messenger notifications
package com.example.instagram_messenger;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;


public class NLService extends NotificationListenerService {

    private String TAG = "instagram_messengerNotificationListener";
    int NotificationID = 0;

    public void createNotification(String NTitle, String NText) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(NTitle)
                        .setContentText(NText)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setChannelId("instagram_messengerNotificationChannel");

        //open app when clicked
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.instagram_messenger");
        PendingIntent pIntent = PendingIntent.getActivity(this, 0,intent, FLAG_IMMUTABLE);
        mBuilder.setContentIntent(pIntent);


        postNotification(mBuilder.build());
    }

    public void postNotification(Notification notification) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationID, notification);
        NotificationID++;
    }
    public void postNotification(Notification notification,int overrideNotificationID) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(overrideNotificationID, notification);
    }

    public void repostNotification(StatusBarNotification sbn) {
        //replace instagram notification with instagram Messenger notification

        Notification notificationOld = sbn.getNotification();

                NotificationCompat.Builder mBuilder = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    mBuilder = new NotificationCompat.Builder(this)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setSmallIcon(R.drawable.notification_icon)
//                            .setGroup(notificationOld.getGroup())
//                            .setGroupAlertBehavior(notificationOld.getGroupAlertBehavior())
                            .setShortcutId(notificationOld.getShortcutId())
                            .setSortKey(notificationOld.getSortKey())
                            .setBubbleMetadata(NotificationCompat.BubbleMetadata.fromPlatform(notificationOld.getBubbleMetadata()))
                            .setBadgeIconType(notificationOld.getBadgeIconType())
                            .setSettingsText(notificationOld.getSettingsText())
                            .setTimeoutAfter(notificationOld.getTimeoutAfter())
                            .setAutoCancel(true)
                            .setChannelId("instagram_messengerNotificationChannel");
                }

        if(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT) != null) {
            mBuilder.setContentText(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
        }
        if(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE) != null){
            mBuilder.setContentTitle(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString());
        }
        if(sbn.getNotification().extras.get(Notification.EXTRA_LARGE_ICON) != null){
            android.graphics.drawable.Icon temp = (Icon) sbn.getNotification().extras.get(Notification.EXTRA_LARGE_ICON);
            Drawable drawable = temp.loadDrawable(this);
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if(bitmapDrawable.getBitmap() != null) {
                    mBuilder.setLargeIcon(bitmapDrawable.getBitmap());
                }
            }
        }

        //group messages
        String groupID = null;
        if (sbn.getNotification().extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) != null) {
            groupID = "com.example.instagram_messenger."+sbn.getNotification().extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE).toString();
        } else if (sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE) != null) {
            groupID = "com.example.instagram_messenger."+sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString();
        }
        mBuilder.setGroup(groupID);


        //open app when clicked
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.instagram_messenger");
        intent.putExtra("convoName",groupID);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0,intent, FLAG_IMMUTABLE);
        mBuilder.setContentIntent(pIntent);

        //post new notification
        postNotification(mBuilder.build());

        //remove original notification
        cancelNotification(sbn.getKey());

        //group messages
        if(groupID!=null){
            mBuilder.setAutoCancel(true)
                    .setContentTitle(groupID)
                    .setGroupSummary(true);
            postNotification(mBuilder.build(),groupID.hashCode());
        }

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
            channel = new NotificationChannel("instagram_messengerNotificationChannel", "instagram_messenger", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "NLService destroyed!");
    }

    public void printExtras(Bundle bundle){
        if (bundle != null) {
            Log.i("TAG", "********* Extras");
            for (String key : bundle.keySet()) {
                Log.i(TAG, key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
            }
        }
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
//        Log.i(TAG, "********** onNotificationRemoved");

        //remove all notifications in group when one removed
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification notiList[] = notificationManager.getActiveNotifications();
        String group = sbn.getNotification().getGroup();
        if(group == null || !sbn.getPackageName().toString().equals("com.example.instagram_messenger")){
            return;
        }

        for(int i=0;i<notiList.length;i++){
            if(notiList[i].getPackageName().toString().equals("com.example.instagram_messenger")
               && notiList[i].getNotification().getGroup().equals(group)){
                int notiId = notiList[i].getId();
                notificationManager.cancel(notiList[i].getTag(),notiId);
            }

        }

    }

    //need to manually enable a protected setting for this to work
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
            if(sbn.getNotification().tickerText == null || sbn.getPackageName() == null){
//                Log.i(TAG, "********** ignored due to null");
                return;
            }
//            Log.i(TAG, "**********  onNotificationPosted");
//            Log.i(TAG, "PACKAGE:"+sbn.getPackageName()+ "\tDASH NOTIFICATION:" + sbn.toString() + "\tNOTIFICATION" + sbn.getNotification().toString());
//            printExtras(sbn.getNotification().extras); //Log.i

            String tickerTextStr = sbn.getNotification().tickerText.toString();
            String packageName = sbn.getPackageName().toString();
            if (tickerTextStr.equals("Instagram\nYou have unseen notifications.")) {
//                Log.i(TAG, "********** delete");
                //remove annoying notification
                cancelNotification(sbn.getKey());

            } else if ((packageName.equals("com.android.chrome") && tickerTextStr.startsWith("Instagram"))
                    || (packageName.equals("com.instagram.android"))){
//                Log.i(TAG, "********** repost");
                repostNotification(sbn);
            }
//            else{
//                Log.i(TAG, "********** ignored");
//            }
    }


}