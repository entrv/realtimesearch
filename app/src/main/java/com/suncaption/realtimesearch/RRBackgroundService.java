package com.suncaption.realtimesearch;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Map;

import static android.app.Service.START_STICKY;

public class RRBackgroundService extends Service {
    private boolean isRunning;
    private Thread backgroundThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        int NOTIFICATION_ID = (int) (System.currentTimeMillis()%10000);
        String NOTIFICATION_CHANNEL_ID = "realrankDummy";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.rk_chart)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        int NOTIFICATION_ID = (int) (System.currentTimeMillis()%10000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
        } else {

           // startForeground(NOTIFICATION_ID,
           //         new Notification.Builder(this, "realrankDummy").build());
        }
        this.isRunning = false;
        this.backgroundThread = new Thread(myTask);
        if (!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }
    }



    private Runnable myTask = new Runnable() {
        public void run() {
            // Do something here
            SharedPreferences preferences = getSharedPreferences("realRank", Activity.MODE_PRIVATE);
            if (preferences != null) {
                Map<String, ?> memoryMap = preferences.getAll();
                Log.e(MainActivity.TAG,"IN THREAD : memoryMap# = " + memoryMap.size());
                new RRAsyncTask(getApplicationContext(), memoryMap).execute();
            }
            stopSelf();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        this.isRunning = false;
    }
}
