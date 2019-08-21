package com.suncaption.realtimesearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static InterstitialAd adFull;
    public static final String TAG = "entrv";
    // FrameLayout에 각 메뉴의 Fragment를 바꿔 줌
    private FragmentManager fragmentManager = getSupportFragmentManager();
    // 4개의 메뉴에 들어갈 Fragment들

    private NaverFragment naverFragment = new NaverFragment();
    private DaumFragment daumFragment = new DaumFragment();
    private AddNewFragment addNewFragment = new AddNewFragment();
    private MelonFragment melonFragment = new MelonFragment();
    private MnetFragment mnetFragment = new MnetFragment();
    private BugsFragment bugsFragment = new BugsFragment();
    private GenieFragment genieFragment = new GenieFragment();
    private SoribadaFragment soribadaFragment = new SoribadaFragment();
    private BillboardFragment billboardFragment = new BillboardFragment();
    private FloFragment floFragment = new FloFragment();
    Fragment active = melonFragment;
    BottomNavigationView bottomNavigationView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

        int id = item.getItemId();

//        if (id == R.id.action_settings) {
//            startActivity(new Intent(MainActivity.this, SettingActivity.class));
//            return true;
//        }
        if (id == R.id.action_site_choice) {
            Intent sitecheck_intent = new Intent(getApplicationContext(), SiteCheckActivity.class);
            startActivity(sitecheck_intent);
            return true;
        }
        if (id == R.id.action_site_soribada) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, soribadaFragment).commitAllowingStateLoss();
            //transaction.remove(soribadaFragment).add(R.id.frame_layout, soribadaFragment).commitAllowingStateLoss();
            return true;
        }
        if (id == R.id.action_site_billboard) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, billboardFragment).commitAllowingStateLoss();
            //transaction.remove(billboardFragment).add(R.id.frame_layout, billboardFragment).commitAllowingStateLoss();
            return true;
        }
        if (id == R.id.action_site_flo) {


            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, floFragment).commitAllowingStateLoss();
            //transaction.replace(R.id.frame_layout, new FloFragment()).commitAllowingStateLoss();
            //transaction.remove(floFragment).add(R.id.frame_layout, floFragment).commitAllowingStateLoss();
            //transaction.hide(active).show(floFragment).commit();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setAlarm();


        SharedPreferences preferences = getApplication()
                .getSharedPreferences("siteCheck", Activity.MODE_PRIVATE);
        Map<String, ?> memoryMap = preferences.getAll();
        int k=0;
        for (Map.Entry<String,?> entry : memoryMap.entrySet()){
            //addMemoryRows(entry.getValue().toString());
            k++;
        }
        if (k == 0 ) {
            SharedPreferences preferences1 = getApplicationContext().getSharedPreferences("siteCheck", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences1.edit();
            editor.putString("ROW"+0,"멜론");
            editor.putString("ROW"+1,"엠넷");
            editor.commit();

        }

        // if exists widget update
        int ids[] = AppWidgetManager.getInstance(this).getAppWidgetIds(
                new ComponentName(this, WidgetProvider.class));
        if (ids != null && ids.length > 0) {
            Intent update = new Intent(this, WidgetProvider.class);
            update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            this.sendBroadcast(update);
        }


         bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        // 첫 화면 지정
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, melonFragment).commitAllowingStateLoss();



        // bottomNavigationView의 아이템이 선택될 때 호출될 리스너 등록
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.navigation_menu1: {
                        transaction.replace(R.id.frame_layout, melonFragment).commitAllowingStateLoss();
                        //transaction.remove(melonFragment).add(R.id.frame_layout, melonFragment).commitAllowingStateLoss();

                        break;
                    }
                    case R.id.navigation_menu2: {
                        transaction.replace(R.id.frame_layout, mnetFragment).commitAllowingStateLoss();
                        //transaction.remove(mnetFragment).add(R.id.frame_layout, mnetFragment).commitAllowingStateLoss();

                        break;
                    }
                    case R.id.navigation_menu3: {
                        transaction.replace(R.id.frame_layout, bugsFragment).commitAllowingStateLoss();
                        //transaction.remove(bugsFragment).add(R.id.frame_layout, bugsFragment).commitAllowingStateLoss();

                        break;
                    }
                    case R.id.navigation_menu4: {

                        transaction.replace(R.id.frame_layout, genieFragment).commitAllowingStateLoss();
                        //transaction.remove(genieFragment).add(R.id.frame_layout, genieFragment).commitAllowingStateLoss();

                        break;
                    }
                    case R.id.navigation_menu5: {
                        adFull.show();

                        transaction.replace(R.id.frame_layout, addNewFragment).commitAllowingStateLoss();
                        //transaction.remove(addNewFragment).add(R.id.frame_layout, addNewFragment).commitAllowingStateLoss();

                        break;
                    }

                }

                return true;
            }
        });


        MobileAds.initialize(this, getResources().getString(R.string.banner_ad_unit_id));
        AdView adView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder()

                .build();
        adView.loadAd(adRequest);

        adFull = new InterstitialAd(getApplicationContext());
        AdRequest adRequest2 = new AdRequest.Builder().build(); //새 광고요청

        adFull.setAdUnitId("ca-app-pub-8297558424373117/4105937520");
        adFull.loadAd(adRequest2); //요청한 광고를 load 합니다.

        adFull.setAdListener(new AdListener() { //전면 광고의 상태를 확인하는 리스너 등록
            public void onAdLoaded(){
                if (adFull.isLoaded()) {
                   // adFull.show();
                } else {
                    Log.d("asd", "The interstitial wasn't loaded yet.");
                }
            }
            @Override
            public void onAdClosed() { //전면 광고가 열린 뒤에 닫혔을 때
                AdRequest adRequest3 = new AdRequest.Builder().build(); //새 광고요청
                adFull.loadAd(adRequest3); //요청한 광고를 load 합니다.
            }
        });
    }

    private void setAlarm() {

        Intent alarm = new Intent(this, RRAlarmReceiver.class);

        boolean alarmRunning = (PendingIntent.getBroadcast(this, 4650, alarm,
                PendingIntent.FLAG_NO_CREATE) != null);
        if (!alarmRunning) {
            Log.e(TAG,"START ALARM");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 4650, alarm, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    pendingIntent);
        }
    }
}