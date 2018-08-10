package com.toshiro97.oderfood.helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import com.toshiro97.oderfood.R;

public class NotificationHelper extends ContextWrapper {
    public static final String FOOD_APP_ID = "com.toshiro97.oderfood";
    public static final String FOOD_APP_NAME = "Order food";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        createChanel();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChanel() {
        NotificationChannel foodOrderChanel = new NotificationChannel(FOOD_APP_ID,FOOD_APP_NAME,NotificationManager.IMPORTANCE_DEFAULT);

        foodOrderChanel.enableLights(false);
        foodOrderChanel.enableVibration(true);
        foodOrderChanel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(foodOrderChanel);
    }

    public NotificationManager getManager() {
        if (manager == null){
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        }
        return manager;
    }
    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOrderAppChannelNotification(String title, String body, PendingIntent contentIntent, Uri soundUri){

        return new Notification.Builder(getApplicationContext(), FOOD_APP_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.logochicken)
                .setSound(soundUri)
                .setAutoCancel(false);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOrderAppChannelNotification(String title, String body, Uri soundUri){

        return new Notification.Builder(getApplicationContext(), FOOD_APP_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
}
