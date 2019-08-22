package com.suncaption.realtimesearch;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Map;

public class RRAsyncTask extends AsyncTask<Void, Void, Void> {
    public static final String NAVER_SITE = "N";
    public static final String DAUM_SITE = "D";
    public static final String MELON_SITE = "M";
    public static final String MNET_SITE = "T";
    public static final String BUGS_SITE = "B";

    public static final String  GENIE_SITE= "G";
    public static final String SORIBADA_SITE = "S";
    public static final String BILLBOARD_SITE = "I";
    public static final String FLO_SITE = "F";

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
    private ArrayList<RRListItem> naverArr, daumArr, melonArr,mnetArr,soribadaArr,genieArr,bugsArr,billboardArr,floArr;

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
        this.melonArr = new ArrayList<>();
        this.mnetArr = new ArrayList<>();
        this.soribadaArr = new ArrayList<>();
        this.genieArr = new ArrayList<>();
        this.bugsArr = new ArrayList<>();
        this.billboardArr = new ArrayList<>();
        this.floArr = new ArrayList<>();

    }

    public RRAsyncTask(Context context, RRListAdapter adapter, String site, AsyncTaskCallBack callBack){
        super();
        this.taskContext = context;
        this.taskAdapter = adapter;
        this.taskSite = site;
        this.fromWhere = FROM_APPLICATION;
        this.naverArr = new ArrayList<>();
        this.daumArr = new ArrayList<>();
        this.melonArr = new ArrayList<>();
        this.mnetArr = new ArrayList<>();
        this.soribadaArr = new ArrayList<>();
        this.genieArr = new ArrayList<>();
        this.bugsArr = new ArrayList<>();
        this.billboardArr = new ArrayList<>();
        this.floArr = new ArrayList<>();
        this.callBack = callBack;
    }

    public RRAsyncTask(Context context, Map<String, ?> entry){
        super();
        this.taskContext = context;
        this.taskEntry = entry;
        this.fromWhere = FROM_SERVICE;
        this.naverArr = new ArrayList<>();
        this.daumArr = new ArrayList<>();
        this.melonArr = new ArrayList<>();
        this.mnetArr = new ArrayList<>();
        this.soribadaArr = new ArrayList<>();
        this.genieArr = new ArrayList<>();
        this.bugsArr = new ArrayList<>();
        this.billboardArr = new ArrayList<>();
        this.floArr = new ArrayList<>();
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

                getRealRank(MELON_SITE);
                getRealRank(MNET_SITE);
                getRealRank(GENIE_SITE);
                getRealRank(BUGS_SITE);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(isConnected()) {
            if (fromWhere == FROM_WIDGET) {

                int site_rec = 0;
                ArrayList<RRListItem> arr_first = null;
                SharedPreferences preferences =  taskContext.getSharedPreferences("siteCheck", Activity.MODE_PRIVATE);
                Map<String, ?> memoryMap = preferences.getAll();
                int logo_int = 1;
                for (Map.Entry<String,?> entry : memoryMap.entrySet()) {

                    //addMemoryRows(entry.getValue().toString());
                    Log.d("entrv", "onCreate: " + entry.getValue().toString());
                    if (entry.getValue().toString().equals("멜론")) {
                        site_rec = R.drawable.melon_rec;
                        arr_first = melonArr;
                    }
                    if (entry.getValue().toString().equals("엠넷")) {
                        site_rec = R.drawable.mnet_rec;
                        arr_first = mnetArr;
                    }
                    if (entry.getValue().toString().equals("지니뮤직")) {
                        site_rec = R.drawable.genie_rec;
                        arr_first = genieArr;
                    }
                    if (entry.getValue().toString().equals("벅스")) {
                        site_rec = R.drawable.bugs_rec;
                        arr_first = bugsArr;
                    }

                    Log.d("entrv", "WIDGET_ROW: " + "start===" + arr_first.size() );
                    if (arr_first.size() > 0) {
                        RemoteViews updateViews = new RemoteViews(taskContext.getPackageName(),
                                R.layout.widget_layout);
                        Log.d("entrv", "WIDGET_ROW: " + WIDGET_ROW);
                        updateViews.setImageViewResource(taskContext.getResources().getIdentifier(
                                "logo_" + logo_int
                                , "id"
                                , taskContext.getPackageName()),
                                site_rec);

                        String update_id_name= "";
                        if (logo_int == 1 ) {
                            update_id_name = "naverTV";
                        }
                        if (logo_int == 2 ) {
                            update_id_name = "daumTV";
                        }
                        for (int i = 0; i < WIDGET_ROW; i++) {
                            Log.d("entrv", "naverArr.get(i).getTitle(): " + entry.getValue().toString() +
                                    ">>>"+ arr_first.get(i).getTitle());
                            //Log.d("entrv", "naverArr.get(i).getTitle(): " + mnetArr.get(i).getTitle());


                            updateViews.setTextViewText(taskContext.getResources().getIdentifier(
                                    update_id_name + (i + 1)
                                    , "id"
                                    , taskContext.getPackageName())
                                    , arr_first.get(i).getTitle());

                        }
                        taskAppWidgetManager.updateAppWidget(taskAppWidgetId, updateViews);
                    }
                    logo_int++;
                }
            } else if (fromWhere == FROM_APPLICATION){
                ArrayList<RRListItem> arr = null;
                if (taskSite.equalsIgnoreCase(MELON_SITE)) {
                    arr = melonArr;
                }
                if (taskSite.equalsIgnoreCase(MNET_SITE)) {
                    arr = mnetArr;
                }
                if (taskSite.equalsIgnoreCase(SORIBADA_SITE)) {
                    arr = soribadaArr;
                }
                if (taskSite.equalsIgnoreCase(GENIE_SITE)) {
                    arr = genieArr;
                }
                if (taskSite.equalsIgnoreCase(BUGS_SITE)) {
                    arr = bugsArr;
                }
                if (taskSite.equalsIgnoreCase(BILLBOARD_SITE)) {
                    arr = billboardArr;
                }
                if (taskSite.equalsIgnoreCase(FLO_SITE)) {
                    arr = floArr;
                }

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
                for (RRListItem item : melonArr){
                    sb.append(item.getTitle().replace(" ","")).append("/");
                }
                for (RRListItem item : mnetArr){
                    sb.append(item.getTitle().replace(" ","")).append("/");
                }
                for (RRListItem item : bugsArr){
                    sb.append(item.getTitle().replace(" ","")).append("/");
                }
                for (RRListItem item : genieArr){
                    sb.append(item.getTitle().replace(" ","")).append("/");
                }
                for (RRListItem item : billboardArr){
                    sb.append(item.getTitle().replace(" ","")).append("/");
                }
                for (RRListItem item : floArr){
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
        }else if (MELON_SITE.equalsIgnoreCase(whatSite)){
            melonArr.clear();
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
            try {
                Document document = Jsoup.connect("https://www.melon.com/chart/index.htm")
                        .userAgent(userAgent)
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
                    Elements elements = document.select("tr.lst50");
                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1)+". ");
                        item.setTitle(elements.get(i).select("div.rank01").text().trim());
                        //하락.상승,동일
                        item.setSinger(elements.get(i).select("div.rank02 a").get(0).text().trim());
                        //item.setUrl(elements.get(i).attr("href"));
                        item.setUrl("https://www.melon.com");
                        if ( elements.get(i).select("span.rank_wrap")
                                .attr("title").contains("신규진입")
                                || elements.get(i).select("span.rank_wrap")
                                .attr("title").contains("순위 진입")
                        ) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        } else if ( elements.get(i).select("span.rank_wrap").attr("title").toString()
                                .contains("상승")) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_up));
                        } else if ( elements.get(i).select("span.rank_wrap").attr("title").toString()
                                .contains("하락")) {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_down));
                        } else {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        }

                        item.setUpDownCnt(elements.get(i).select("span.rank_wrap").attr("title").toString()
                        .replace("단계 하락","").replace("단계 상승","").replace("순위 동일",""));

                        item.setThumbnail(elements.get(i).select("a.image_typeAll img").attr("src"));
                        item.setAlbumTitle(elements.get(i).select("div.rank03 a").text());
                        melonArr.add(item);
                    }






                   /* for (RRListItem print : melonArr){
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


                document = Jsoup.connect("https://www.melon.com/chart/index.htm#params%5Bidx%5D=51")
                        .userAgent(userAgent)
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
                    Elements elements = document.select("tr.lst100");
                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1)+50+". ");
                        item.setTitle(elements.get(i).select("div.rank01").text().trim());
                        //하락.상승,동일
                        item.setSinger(elements.get(i).select("div.rank02 a").get(0).text().trim());
                        //item.setUrl(elements.get(i).attr("href"));
                        item.setUrl("https://www.melon.com");
                        if ( elements.get(i).select("span.rank_wrap")
                                .attr("title").contains("신규진입")
                            || elements.get(i).select("span.rank_wrap")
                                .attr("title").contains("순위 진입")

                        ) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        } else if ( elements.get(i).select("span.rank_wrap").attr("title").toString()
                                .contains("상승")) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_up));
                        } else if ( elements.get(i).select("span.rank_wrap").attr("title").toString()
                                .contains("하락")) {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_down));
                        }else {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        }

                        item.setUpDownCnt(elements.get(i).select("span.rank_wrap").attr("title").toString()
                                .replace("단계 하락","").replace("단계 상승","")
                                .replace("순위 동일","")
                                        .replace("순위 진입","")
                                );

                        item.setThumbnail(elements.get(i).select("a.image_typeAll img").attr("src"));
                        item.setAlbumTitle(elements.get(i).select("div.rank03 a").text());
                        melonArr.add(item);
                    }






                   /* for (RRListItem print : melonArr){
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
        }else if (MNET_SITE.equalsIgnoreCase(whatSite)){
            mnetArr.clear();
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
            try {
                Document document = Jsoup.connect("http://www.mnet.com/chart/TOP100/")
                        .userAgent(userAgent)
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
                    Elements elements = document.select("table tbody tr");
                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1)+". ");
                        item.setTitle(elements.get(i).select("div.MMLITitleSong_Box a").get(1).text().trim());
                        //하락.상승,동일
                        item.setSinger(elements.get(i).select("div.MMLITitle_Info a").get(0).text().trim());
                        //item.setUrl(elements.get(i).attr("href"));
                        item.setUrl("http://www.mnet.com/chart/TOP100/");
                        if ( elements.get(i).select("span.MMLI_Updown").text()
                                .contains("보합")

                        ) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        } else if ( elements.get(i).select("span.MMLI_Updown_Up").text().contains("상승")) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_up));
                        } else if ( elements.get(i).select("span.MMLI_Updown_Down").text().contains("상승") ) {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_down));
                        } else {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        }

                        item.setUpDownCnt(elements.get(i).select("span.MMLI_UpdownNum")
                                .text()
                                .replace("단계 하락","")
                                .replace("단계 상승","")
                                .replace("순위 동일",""));

                        item.setThumbnail(elements.get(i).select("div.MMLITitle_Album a img").attr("src"));
                        item.setAlbumTitle(elements.get(i).select("a.MMLIInfo_Album").text());
                        mnetArr.add(item);
                    }






                  /*  for (RRListItem print : melonArr){
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


                document = Jsoup.connect("http://www.mnet.com/chart/TOP100/?pNum=2")
                        .userAgent(userAgent)
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
                    Elements elements = document.select("table tbody tr");
                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1+50)+". ");
                        item.setTitle(elements.get(i).select("div.MMLITitleSong_Box a").get(1).text().trim());
                        //하락.상승,동일
                        item.setSinger(elements.get(i).select("div.MMLITitle_Info a").get(0).text().trim());
                        //item.setUrl(elements.get(i).attr("href"));
                        item.setUrl("http://www.mnet.com/chart/TOP100/");
                        if ( elements.get(i).select("span.MMLI_Updown").text()
                                .contains("보합")

                        ) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        } else if ( elements.get(i).select("span.MMLI_Updown_Up").text().contains("상승")) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_up));
                        } else if ( elements.get(i).select("span.MMLI_Updown_Down").text().contains("상승") ) {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_down));
                        } else {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        }

                        item.setUpDownCnt(elements.get(i).select("span.MMLI_UpdownNum")
                                .text()
                                .replace("단계 하락","")
                                .replace("단계 상승","")
                                .replace("순위 동일",""));

                        item.setThumbnail(elements.get(i).select("div.MMLITitle_Album a img").attr("src"));
                        item.setAlbumTitle(elements.get(i).select("a.MMLIInfo_Album").text());
                        mnetArr.add(item);
                    }






                    /*for (RRListItem print : mnetArr){
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
        }else if (BUGS_SITE.equalsIgnoreCase(whatSite)){
            bugsArr.clear();
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
            try {
                Document document = Jsoup.connect("https://music.bugs.co.kr/chart")
                        .userAgent(userAgent)
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
                    Elements elements = document.select("table.list tbody tr");
                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1)+". ");
                        item.setTitle(elements.get(i).select("p.title a").get(0).text().trim());
                        //하락.상승,동일
                        item.setSinger(elements.get(i).select("p.artist a").get(0).text().trim());
                        //item.setUrl(elements.get(i).attr("href"));
                        item.setUrl("https://www.bugs.co.kr");
                        if ( elements.get(i).select("p.none").text()
                                .contains("변동없음")

                        ) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        } else if ( elements.get(i).select("p.up").text().contains("상승")) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_up));
                        } else if ( elements.get(i).select("p.down").text().contains("하락") ) {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_down));
                        } else {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        }

                        item.setUpDownCnt(elements.get(i).select("p.change em")
                                .text()
                                .replace("단계 하락","")
                                .replace("단계 상승","")
                                .replace("순위 동일",""));

                        item.setThumbnail(elements.get(i).select("td a.thumbnail img").attr("src"));
                        item.setAlbumTitle(elements.get(i).select("a.album").text());

                        bugsArr.add(item);
                    }






                   /* for (RRListItem print : bugsArr){
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
        }
        else if (GENIE_SITE.equalsIgnoreCase(whatSite)){
            genieArr.clear();
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
            try {
                Document document = Jsoup.connect("https://www.genie.co.kr/chart/top200")
                        .userAgent(userAgent)
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
                    Elements elements = document.select("table.list-wrap tbody tr");
                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1)+". ");
                        item.setTitle(elements.get(i).select("a.title").get(0).text().trim());
                        //하락.상승,동일
                        item.setSinger(elements.get(i).select("a.artist").get(0).text().trim());
                        //item.setUrl(elements.get(i).attr("href"));
                        item.setUrl("https://www.genie.co.kr");
                        if ( elements.get(i).select("span.rank-none").text()
                                .contains("유지")

                        ) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        } else if ( elements.get(i).select("span.rank-up").text().contains("상승")) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_up));
                        } else if ( elements.get(i).select("span.rank-down").text().contains("하강") ) {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_down));
                        } else {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        }

                        item.setUpDownCnt(elements.get(i).select("span.rank").get(1)
                                .text()
                                .replace("하강","")
                                .replace("상승","")
                                .replace("유지",""));

                        item.setThumbnail("http:" +
                                elements.get(i).select("td a.cover img")
                                        .attr("src"));
                        item.setAlbumTitle(elements.get(i).select("a.albumtitle").text());

                        genieArr.add(item);
                    }






                   /* for (RRListItem print : genieArr){
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

                document = Jsoup.connect("https://www.genie.co.kr/chart/top200?&rtm=Y&pg=2")
                        .userAgent(userAgent)
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
                    Elements elements = document.select("table.list-wrap tbody tr");
                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1+50)+". ");
                        item.setTitle(elements.get(i).select("a.title").get(0).text().trim());
                        //하락.상승,동일
                        item.setSinger(elements.get(i).select("a.artist").get(0).text().trim());
                        //item.setUrl(elements.get(i).attr("href"));
                        item.setUrl("https://www.genie.co.kr");
                        if ( elements.get(i).select("span.rank-none").text()
                                .contains("유지")

                        ) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        } else if ( elements.get(i).select("span.rank-up").text().contains("상승")) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_up));
                        } else if ( elements.get(i).select("span.rank-down").text().contains("하강") ) {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_down));
                        } else {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        }

                        item.setUpDownCnt(elements.get(i).select("span.rank").get(1)
                                .text()
                                .replace("하강","")
                                .replace("상승","")
                                .replace("유지",""));

                        item.setThumbnail("http:/" +
                                elements.get(i).select("td a.cover img")
                                        .attr("src"));
                        item.setAlbumTitle(elements.get(i).select("a.albumtitle").text());
                        genieArr.add(item);
                    }






                    /*for (RRListItem print : genieArr){
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
        }
        else if (SORIBADA_SITE.equalsIgnoreCase(whatSite)){
            soribadaArr.clear();
            Log.d("entrv", "getRealRank: ");
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
            try {
                Document document = Jsoup.connect("http://sbapi.soribada.com/charts/songs/realtime/json/?callback=&page=1&size=100&vid=0&version=2.5&device=web&favorite=true&cachetype=charts_songs_realtime&authKey=")
                        .userAgent(userAgent)
                        .header("Connection", "close")
                        .header("Accept-Encoding", "identity")
                        .ignoreContentType(true)
                        .get();
                if (document != null) {
                    try {

                        JSONObject jsonObject = new JSONObject(document.text());
                        JSONObject jsonObject2 = jsonObject.getJSONObject("SoribadaApiResponse");
                        JSONObject jsonObject3 = jsonObject2.getJSONObject("Songs");
                        JSONArray items = jsonObject3.getJSONArray("Song");
                        Log.d("enrv", "items.length(): " + items.length());
                        for(int i=0; i<items.length(); i++) {
                            JSONObject item_object = (JSONObject) items.get(i);
                            String songTitle = item_object.getString("Name");
                            JSONObject Artists =(JSONObject) item_object.get("Artists");
                            JSONArray Artists2 =(((JSONArray) Artists.get("Artist")));


                            String songSinger =  (String) ((JSONObject)Artists2.get(0)).get("Name");

                            String songUpDown =  item_object.getString("PreRank");



                            JSONObject album =(JSONObject) item_object.get("Album");
                            String songAlbumTitle = (String) album.get("Name") ;

                            JSONObject Pictures =(JSONObject) album.get("Pictures");
                            JSONArray album_thumb =(((JSONArray) Pictures.get("Picture")));
                            JSONObject album_thumb2 =(JSONObject) album_thumb.get(1);
                            String songThumbnail = (String) album_thumb2.get("URL");

                            //String item2 = (String) (((JSONArray) items.get(i)).get(0));
                            Log.d("enrv", "getRealRank: " + songThumbnail + "" + songAlbumTitle);

                            item = new RRListItem();
                            item.setRank((i+1)+". ");
                            item.setTitle(songTitle.trim());
                            //하락.상승,동일
                            item.setSinger(songSinger.trim());
                            //item.setUrl(elements.get(i).attr("href"));
                            item.setUrl("http://www.soriba.com");

                            if (Integer.parseInt(songUpDown) == 0) {
                                item.setUpDown(0);
                                item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                            } else if (Integer.parseInt(songUpDown) > 0) {
                                item.setUpDown(1);
                                item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_up));
                            } else  if (Integer.parseInt(songUpDown) < 0) {
                                item.setUpDown(-1);
                                item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_down));
                            } else {
                                item.setUpDown(0);
                                item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                            }



                            item.setUpDownCnt(songUpDown);

                            item.setThumbnail(
                                    songThumbnail);
                            item.setAlbumTitle(songAlbumTitle);

                            soribadaArr.add(item);
                        }


                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + document.text() + "\"");
                    }
                    // www.naver.com
                    // id가 realrank 인 ol 태그 아래 id가 lastrank인 li 태그를 제외한 모든 li 안에 존재하는 a 태그의 내용을 가져옵니다.
                    // "ol#realrank > li:not(#lastrank) > a"
                    // 2017.03.27 네이버 개편 : li 태크중 data-order를 가진 하위 a / ah_r / ah_k / ah_icon / ah_s
                    // 2017.03.29 네이버 챠트 개편 :
                    //            li[data-order]>a.ah_a : ah_r 랭킹 : ah_k 타이틀 : href 링크
                    //            li[data-order]>a.ah_da : href 링크
                    /*Elements elements = document.select("ul.music-list");
                    for (int i = 0; i < elements.size(); i++) {

                    }*/






                    for (RRListItem print : soribadaArr){
                        System.out.println("검색어 : " + print.getTitle());
                        System.out.println("랭킹 : " + print.getRank());
                        System.out.println("상승여부 : " + print.getUpDown()); //상승 NEW
                        System.out.println("상승단계 : " + print.getUpDownCnt());
                        System.out.println("링크 URL : " + print.getUrl());
                        System.out.println("차트 URL : " + print.getUrlChart());
                        System.out.println("------------------------------------------");
                    }

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
        }
        else if (BILLBOARD_SITE.equalsIgnoreCase(whatSite)){
            billboardArr.clear();
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
            try {
                Document document = Jsoup.connect("https://www.billboard.com/charts/hot-100")
                        .userAgent(userAgent)
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
                    Elements elements = document.select("div.chart-list-item");
                    for (int i = 0; i < elements.size(); i++) {
                        item = new RRListItem();
                        item.setRank((i+1)+". ");
                        item.setTitle(elements.get(i).select("span.chart-list-item__title-text").get(0).text().trim());
                        //하락.상승,동일
                        item.setSinger(elements.get(i).select("div.chart-list-item__artist").get(0).text().trim());
                        //item.setUrl(elements.get(i).attr("href"));
                        item.setUrl("https://www.billboard.com/charts/hot-100");
                        String last_week = elements.get(i).select("div.chart-list-item__last-week").get(0).text().trim();
                        last_week = last_week.replace("-","");
                        if (last_week.equals("")) {
                            last_week = "0";
                        }
                        int upDown = Integer.parseInt(last_week) - (i+1);
                        int upDownCnt = upDown;
                        if (upDownCnt < 0 ) {
                            upDownCnt = upDownCnt * -1;
                        }
                        if (upDown == 0) {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        } else if (upDown > 0) {
                            item.setUpDown(1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_up));
                        } else if (upDown < 0) {
                            item.setUpDown(-1);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_down));
                        } else {
                            item.setUpDown(0);
                            item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                        }


                        item.setUpDownCnt(String.valueOf(upDownCnt));

                        item.setThumbnail(
                                elements.get(i).select("div.chart-list-item__image-wrapper img")
                                        .attr("data-src"));
                        item.setAlbumTitle("");

                        billboardArr.add(item);
                    }






                   /* for (RRListItem print : genieArr){
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
            }catch (Exception e) {
                e.printStackTrace();
            }

        }
        else if (FLO_SITE.equalsIgnoreCase(whatSite)) {
            floArr.clear();
            Log.d("entrv", "getRealRank: ");
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
            try {
                Document document = Jsoup.connect("https://music-flo.com/api/meta/v1/chart/track/1")
                        .userAgent(userAgent)
                        .header("Connection", "close")
                        .header("Accept-Encoding", "identity")
                        .ignoreContentType(true)
                        .get();
                if (document != null) {
                    try {

                        JSONObject jsonObject = new JSONObject(document.text());
                        JSONObject jsonObject2 = jsonObject.getJSONObject("data");

                        JSONArray items = jsonObject2.getJSONArray("trackList");
                        Log.d("enrv", "items.length(): " + items.length());
                        for(int i=0; i<items.length(); i++) {
                            JSONObject item_object = (JSONObject) items.get(i);
                            String songTitle = item_object.getString("name");

                            JSONArray Artists2 =(((JSONArray) item_object.get("artistList")));


                            String songSinger =  (String) ((JSONObject)Artists2.get(0)).get("name");

                            //String songUpDown =  (String) ((JSONObject)item_object.get("rank")).get("rankBadge");

                            int songUpDown_int = (int) ((JSONObject)item_object.get("rank")).get("rankBadge");
                            String songUpDown =  String.valueOf(songUpDown_int);
                            JSONObject album =(JSONObject) item_object.get("album");
                            String songAlbumTitle = (String) album.get("title") ;


                            JSONArray album_thumb =(((JSONArray) album.get("imgList")));
                            JSONObject album_thumb2 =(JSONObject) album_thumb.get(0);
                            String songThumbnail = (String) album_thumb2.get("url");

                            //String item2 = (String) (((JSONArray) items.get(i)).get(0));
                            Log.d("enrv", "getRealRank: " + songThumbnail + "" + songAlbumTitle);

                            item = new RRListItem();
                            item.setRank((i+1)+". ");
                            item.setTitle(songTitle.trim());
                            //하락.상승,동일
                            item.setSinger(songSinger.trim());
                            //item.setUrl(elements.get(i).attr("href"));
                            item.setUrl("http://www.soriba.com");

                            if (Integer.parseInt(songUpDown) == 0) {
                                item.setUpDown(0);
                                item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                            } else if (Integer.parseInt(songUpDown) > 0) {
                                item.setUpDown(1);
                                item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_up));
                            } else  if (Integer.parseInt(songUpDown) < 0) {
                                item.setUpDown(-1);
                                item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_down));
                            } else {
                                item.setUpDown(0);
                                item.setUpdownImg(ContextCompat.getDrawable(taskContext, R.drawable.rank_bar));
                            }



                            item.setUpDownCnt(songUpDown);

                            item.setThumbnail(
                                    songThumbnail);
                            item.setAlbumTitle(songAlbumTitle);

                            floArr.add(item);
                        }


                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + document.text() + "\"");
                    }
                    // www.naver.com
                    // id가 realrank 인 ol 태그 아래 id가 lastrank인 li 태그를 제외한 모든 li 안에 존재하는 a 태그의 내용을 가져옵니다.
                    // "ol#realrank > li:not(#lastrank) > a"
                    // 2017.03.27 네이버 개편 : li 태크중 data-order를 가진 하위 a / ah_r / ah_k / ah_icon / ah_s
                    // 2017.03.29 네이버 챠트 개편 :
                    //            li[data-order]>a.ah_a : ah_r 랭킹 : ah_k 타이틀 : href 링크
                    //            li[data-order]>a.ah_da : href 링크
                    /*Elements elements = document.select("ul.music-list");
                    for (int i = 0; i < elements.size(); i++) {

                    }*/






                    for (RRListItem print : soribadaArr){
                        System.out.println("검색어 : " + print.getTitle());
                        System.out.println("랭킹 : " + print.getRank());
                        System.out.println("상승여부 : " + print.getUpDown()); //상승 NEW
                        System.out.println("상승단계 : " + print.getUpDownCnt());
                        System.out.println("링크 URL : " + print.getUrl());
                        System.out.println("차트 URL : " + print.getUrlChart());
                        System.out.println("------------------------------------------");
                    }

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
        }
    }
}
