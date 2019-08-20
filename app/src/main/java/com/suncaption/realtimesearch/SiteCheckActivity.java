package com.suncaption.realtimesearch;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

public class SiteCheckActivity extends AppCompatActivity {
    private String TAG = "ROW";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_check);
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final CheckBox cb1 = (CheckBox)findViewById(R.id.checkBox1);
        final CheckBox cb2 = (CheckBox)findViewById(R.id.checkBox2);
        final CheckBox cb3 = (CheckBox)findViewById(R.id.checkBox3);
        final CheckBox cb4 = (CheckBox)findViewById(R.id.checkBox4);


        SharedPreferences preferences = getApplication()
                .getSharedPreferences("siteCheck", Activity.MODE_PRIVATE);
        Map<String, ?> memoryMap = preferences.getAll();
        for (Map.Entry<String,?> entry : memoryMap.entrySet()){
            //addMemoryRows(entry.getValue().toString());
            Log.d("entrv", "onCreate: " + entry.getValue().toString());
            if (entry.getValue().toString().equals("멜론")) {
                cb1.setChecked(true);
            }
            if (entry.getValue().toString().equals("엠넷")) {
                cb2.setChecked(true);
            }
            if (entry.getValue().toString().equals("지니뮤직")) {
                cb3.setChecked(true);
            }
            if (entry.getValue().toString().equals("벅스")) {
                cb4.setChecked(true);
            }
        }

        Button b = (Button)findViewById(R.id.button1);
        //final TextView tv = (TextView)findViewById(R.id.textView2);

        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String result = "";  // 결과를 출력할 문자열  ,  항상 스트링은 빈문자열로 초기화 하는 습관을 가지자
                int check_length = 0;
                if(cb1.isChecked() == true) {
                    check_length += 1;
                    result += cb1.getText().toString();
                }
                if(cb2.isChecked() == true) {
                    check_length += 1;
                    result += cb2.getText().toString();
                }
                if(cb3.isChecked() == true){
                    check_length += 1;
                    result += cb3.getText().toString();
                }
                if(cb4.isChecked() == true) {
                    check_length += 1;
                    result += cb4.getText().toString();
                }

                if (check_length != 2) {
                    Toast.makeText(SiteCheckActivity.this, "2 개만 체크 가능 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences preferences = getApplicationContext().getSharedPreferences("siteCheck", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear().commit();

                if(cb1.isChecked() == true) {

                    editor.putString(TAG+0,cb1.getText().toString());
                }
                if(cb2.isChecked() == true) {

                    editor.putString(TAG+1,cb2.getText().toString());
                }
                if(cb3.isChecked() == true) {

                    editor.putString(TAG+2,cb3.getText().toString());
                }
                if(cb4.isChecked() == true) {

                    editor.putString(TAG+3,cb4.getText().toString());
                }
                editor.commit();

                Toast.makeText(SiteCheckActivity.this, "설정 되었습니다.", Toast.LENGTH_SHORT).show();
                return;

                //tv.setText("선택결과:" + result);

            } // end onClick
        }); // end setOnClickListener

    }
}
