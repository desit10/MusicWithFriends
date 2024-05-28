package com.example.musicwithfriends.Services;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class MediaNotificationListenerService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        Log.i("onNotificationPosted", String.valueOf(sbn.getId()));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Log.i("onNotificationRemoved", String.valueOf(sbn.getId()));
    }

}
