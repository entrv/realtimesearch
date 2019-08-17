package com.suncaption.realtimesearch;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Map;

import static android.app.Service.START_STICKY;

public class RRBackgroundService extends Service {
    private boolean isRunning;
    private Thread backgroundThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int NOTIFICATION_ID = (int) (System.currentTimeMillis()%10000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, new Notification.Builder(this, "realrankDummy").build());
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
