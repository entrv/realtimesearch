package com.suncaption.realtimesearch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0 ; i < appWidgetIds.length ; i++){
            updateRealRank(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    private void updateRealRank(Context context, AppWidgetManager appWidgetManager, int appWidgetId){
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // 위젯 클리시 mainActivity 호출
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_body, pendingIntent);

        // 위젯 상단 클릭시 onUpdate() 호출
        Intent intentR = new Intent(context, WidgetProvider.class);
        intentR.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = new int[]{appWidgetId};
        intentR.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        PendingIntent pendingIntentR = PendingIntent.getBroadcast(context,
                (appWidgetId * -1), intentR, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_title, pendingIntentR);

        // remoteViews 업데이트
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        // 위젯 정보 갱신
        //Log.e(MainActivity.TAG, "CALL WIDGET UPDATE");
        RRAsyncTask task = new RRAsyncTask(context, appWidgetManager, appWidgetId);
        task.execute();
    }
}
