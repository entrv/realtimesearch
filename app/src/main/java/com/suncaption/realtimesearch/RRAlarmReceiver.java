package com.suncaption.realtimesearch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class RRAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(MainActivity.TAG,"CALL SERVICE");
        Intent background = new Intent(context, RRBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(background);
        } else {
            context.startService(background);
        }
    }
}
