//https://stackoverflow.com/questions/60870820/permanently-running-notification-listener
package com.example.instagramx
import android.util.Log

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d("NotificationListenerTesting", "id = " + sbn.getId() + "Package Name" + sbn.getPackageName() +
                "Post time = " + sbn.getPostTime() + "Tag = " + sbn.getTag());
    }
}