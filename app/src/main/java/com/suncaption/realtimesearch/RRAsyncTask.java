package com.suncaption.realtimesearch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class RRAsyncTask extends AsyncTask<Void, Void, Void> {
    public static final String NAVER_SITE = "N";
    public static final String DAUM_SITE = "D";
    private static final int FROM_WIDGET = 0;
    private static final int FROM_APPLICATION = 1;
    private static final int FROM_SERVICE = 2;
    private static final int WIDGET_ROW = 10;

    //for widget
    private Context taskContext;
    private AppWidgetManager taskAppWidgetManager;
    private int taskAppWidgetId;

    //for app
    RRListAdapter taskAdapter;
    String taskSite;

    //http parsing
    private int fromWhere;
    private ArrayList<RRListItem> naverArr, daumArr;

    //service
    private Map<String, ?> taskEntry;

    private AsyncTaskCallBack callBack;

    public RRAsyncTask(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        super();
        this.taskContext = context;
        this.taskAppWidgetManager = appWidgetManager;
        this.taskAppWidgetId = appWidgetId;
        this.fromWhere = FROM_WIDGET;
        this.naverArr = new ArrayList<>();
        this.daumArr = new ArrayList<>();
    }

    public RRAsyncTask(Context context, RRListAdapter adapter, String site, AsyncTaskCallBack callBack){
        super();
        this.taskContext = context;
        this.taskAdapter = adapter;
        this.taskSite = site;
        this.fromWhere = FROM_APPLICATION;
        this.naverArr = new ArrayList<>();
        this.daumArr = new ArrayList<>();
        this.callBack = callBack;
    }

    public RRAsyncTask(Context context, Map<String, ?> entry){
        super();
        this.taskContext = context;
        this.taskEntry = entry;
        this.fromWhere = FROM_SERVICE;
        this.naverArr = new ArrayList<>();
        this.daumArr = new ArrayList<>();
    }

    private boolean isConnected() {
        boolean isConnected;
        ConnectivityManager conn = (ConnectivityManager) taskContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

        return isConnected;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(isConnected()) {
            if (fromWhere == FROM_APPLICATION) {
                getRealRank(taskSite);
            } else {
                getRealRank(NAVER_SITE);
                getRealRank(DAUM_SITE);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(isConnected()) {
            if (fromWhere == FROM_WIDGET) {
                Log.d("entrv", "WIDGET_ROW: " + "start===" + naverArr.size() +"==="+ daumArr.size());
                if (naverArr.size() > 0 && daumArr.size() > 0) {
                    RemoteViews updateViews = new RemoteViews(taskContext.getPackageName(),
                            R.layout.widget_layout);
                    Log.d("entrv", "WIDGET_ROW: " + WIDGET_ROW);
                    for (int i = 0; i < WIDGET_ROW; i++) {
                        Log.d("entrv", "naverArr.get(i).getTitle(): " + naverArr.get(i).getTitle());
                        Log.d("entrv", "naverArr.get(i).getTitle(): " + daumArr.get(i).getTitle());


                        updateViews.setTextViewText(taskContext.getResources().getIdentifier(
                                "naverTV" + (i + 1)
                                ,"id"
                                , taskContext.getPackageName())
                                , naverArr.get(i).getTitle());
                        updateViews.setTextViewText(taskContext.getResources().getIdentifier(
                                "daumTV" + (i + 1)
                                ,"id"
                                , taskContext.getPackageName())
                                , daumArr.get(i).getTitle());
                    }
                    taskAppWidgetManager.updateAppWidget(taskAppWidgetId, updateViews);
                }
            } else if (fromWhere == FROM_APPLICATION){
                ArrayList<RRListItem> arr = (taskSite.equalsIgnoreCase(NAVER_SITE) ? naverArr : daumArr);
                if (arr.size() > 0) {
                    taskAdapter.getListItemList().clear();
                    for (int i = 0; i < arr.size(); i++) {
                        taskAdapter.addItem(arr.get(i));
                    }
                    taskAdapter.notifyDataSetChanged();
                }
            } else if (fromWhere == FROM_SERVICE){
                // widget update
                int ids[] = AppWidgetManager.getInstance(taskContext).getAppWidgetIds(new ComponentName(taskContext, WidgetProvider.class));
                if (ids != null && ids.length > 0) {
                    Intent update = new Intent(taskContext, WidgetProvider.class);
                    update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                    taskContext.sendBroadcast(update);
                }

                //check and call notification
                StringBuilder sb = new StringBuilder();
                String target = "";
                for (RRListItem item : naverArr){
                    sb.append(item.getTitle().replace(" ","")).append("/");
                }
                for (RRListItem item : daumArr){
                    sb.append(item.getTitle().replace(" ","")).append("/");
                }
                for (Map.Entry<String,?> entry : taskEntry.entrySet()){
                    if (sb.toString().contains(entry.getValue().toString().replace(" ",""))){
                        target = entry.getValue().toString();
                        break;
                    }
                }
                if (!"".equalsIgnoreCase(target)){
                    //notification
                    Log.e(MainActivity.TAG,"CALL NOTIFICATION : matching = " + target);
                    Resources res = taskContext.getResources();
                    Intent notificationIntent = new Intent(taskContext, MainActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(taskContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationManager notificationManager = (NotificationManager) taskContext.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel channel = new NotificationChannel("realeankChannel", "RealRank Channel", importance);
                        notificationManager.createNotificationChannel(channel);
                    }

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(taskContext, "realeankChannel");
                    builder.setContentTitle(res.getString(R.string.app_name))
                            .setContentText("["+target+"] "+
                                    res.getString(R.string.ranking_text_show)
                                    + res.getString(R.string.ranking_text_push))
                            .setTicker(res.getString(R.string.ranking_text_push))
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setWhen(System.currentTimeMillis())
                            .setDefaults(Notification.DEFAULT_ALL);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        builder.setCategory(Notification.CATEGORY_MESSAGE)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setVisibility(Notification.VISIBILITY_PUBLIC);
                    }

                    notificationManager.notify(160604,builder.build());
                }
            }
        } else {
            Toast.makeText(taskContext, taskContext.getString(R.string.check_network_toast), Toast.LENGTH_SHORT).show();
        }
        if (callBack != null) {
            callBack.onSuccess();
        }
    }

    private void getRealRank(String whatSite){
        RRListItem item;
        if (NAVER_SITE.equalsIgnoreCase(whatSite)){
            naverArr.clear();
            try {
                Document document = Jsoup.connect("http://www.naver.com")
                .header("Connection", "close")
                        .header("Accept-Encoding", "identity").get();
                if (document != null) {
                    // www.naver.com
                    // id가 realrank 인 ol 태그 아래 id가 lastrank인 li 태그를 제외한 모든 li 안에 존재하는 a 태그의 내용을 가져옵니다.
                    // "ol#realrank > li:not(#lastrank) > a"
                    // 2017.03.27 네이버 개편 : li 태크중 data-order를 가진 하위 a / ah_r / ah_k / ah_icon / ah_s
                    // 2017.03.29 네이버 챠트 개편 :
                    //            li[data-order]>a.ah_a : ah_r 랭킹 : ah_k 타이틀 : href 링크
                    //            li[data-order]>a.ah_da : href 링크
                    Elements elements = document.select("li[data-order]>a.ah_a");
                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1)+". ");
                        item.setTitle(elements.get(i).select("span.ah_k").text());
                        item.setUrl(elements.get(i).attr("href"));
                        naverArr.add(item);
                    }

                    Elements elementsChart = document.select("li[data-order]>a.ah_da");
                    for (int i = 0; i < elementsChart.size(); i++) {
                        item = naverArr.get(i);
                        item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rk_chart));
                        item.setUrlChart(elementsChart.get(i).attr("href"));
                    }

                    /*for (RRListItem print : naverArr){
                        System.out.println("검색어 : " + print.getTitle());
                        System.out.println("랭킹 : " + print.getRank());
                        System.out.println("상승여부 : " + print.getUpDown()); //상승 NEW
                        System.out.println("상승단계 : " + print.getUpDownCnt());
                        System.out.println("링크 URL : " + print.getUrl());
                        System.out.println("차트 URL : " + print.getUrlChart());
                        System.out.println("------------------------------------------");
                    }*/

                    /*for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1)+". ");
                        item.setTitle(elements.get(i).select("span.ah_k").text());
                        if (elements.get(i).select("span.ah_icon").text().contains("NEW")) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rk_new));
                            item.setUpDownCnt("");
                        } else if (elements.get(i).select("span.ah_icon").text().contains("상승")) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rk_up));
                            item.setUpDownCnt(elements.get(i).select("span.ah_s").text());
                        } else {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rk_down));
                            item.setUpDownCnt(elements.get(i).select("span.ah_s").text());
                        }
                        item.setUrl(elements.get(i).attr("href"));
                        naverArr.add(item);

                        *//*System.out.println("검색어 : " + elements.get(i).select("span.ah_k").text());
                        System.out.println("랭킹 : " + (i + 1));
                        System.out.println("상승여부 : " + elements.get(i).select("span.ah_icon").text()); //상승 NEW
                        System.out.println("상승단계 : " + elements.get(i).select("span.ah_s").text());
                        System.out.println("링크 URL : " + elements.get(i).attr("href"));
                        System.out.println("------------------------------------------");*//*
                    }*/
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (DAUM_SITE.equalsIgnoreCase(whatSite)) {
            daumArr.clear();
            try {
                Document document = Jsoup.connect("http://www.daum.net")
                        .header("Connection", "close")
                        .header("Accept-Encoding", "identity").get();

                if (document != null) {
                    // class 가 rank_dummy 를 가지고 있는 div 태그는 포함시키지 않는다.
                    // <em class="num_issue"><span class="img_vert txt_num ico_up"><span class="ir_wa">&uarr; 상승</span></span>282</em>
                    // 위 HTML 에서 상승단계를 구하기 위해서 상승여부를 제거합니다.
                    // 2017.04.05 daum 사이트 개편 : div.rank_cont[aria-hidden] 하위 span.txt_issue
                    // 2017.06.29 daum 사이트 개편 : hotissue_layer 하위의 rank_cont를 가져온다
                    Elements elements = document.select("div.hotissue_layer").select("div.rank_cont[aria-hidden]");
                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1)+". ");
                        item.setTitle(elements.get(i).select("span.txt_issue").text());
                        item.setUrl(elements.get(i).select("span.txt_issue>a").attr("href"));
                        if (elements.get(i).select("em.rank_result").text().contains("신규진입")) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rk_new));
                        } else if (elements.get(i).select("em.rank_result").text().contains("상승")) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rk_up));
                        } else {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rk_down));
                        }

                        /*System.out.println("랭킹 : " + (i + 1));
                        System.out.println("검색어 : " + elements.get(i).select("span.txt_issue").text());
                        System.out.println("링크 URL : " + elements.get(i).select("span.txt_issue>a").attr("href"));
                        System.out.println("상승여부 : " + elements.get(i).select("em.rank_result").text());
*/
                        elements.get(i).select("em.rank_result>span.ico_pctop").remove();
                        item.setUpDownCnt(elements.get(i).select("em.rank_result").text().replace("신규진입",""));
                        daumArr.add(item);

                        /*System.out.println("상승단계 : " + elements.get(i).select("em.rank_result").text());
                        System.out.println("------------------------------------------");*/
                    }


                    /*Elements elements = document.select("ol#realTimeSearchWord > li > div.roll_txt > div:not(.rank_dummy)");

                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1)+". ");
                        item.setTitle(elements.get(i).select("span.txt_issue > a").text());
                        if (elements.get(i).select("em.num_issue > span").text().contains("신규진입")) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rk_new));
                        } else if (elements.get(i).select("em.num_issue > span").text().contains("상승")) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rk_up));
                        } else {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rk_down));
                        }
                        elements.get(i).select("span.img_vert").remove();
                        item.setUpDownCnt(elements.get(i).select("em.num_issue").html());
                        item.setUrl(elements.get(i).select("span.txt_issue > a").attr("href"));
                        daumArr.add(item);
                    }*/
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
