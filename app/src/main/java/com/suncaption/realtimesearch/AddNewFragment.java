package com.suncaption.realtimesearch;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Map;

public class AddNewFragment extends Fragment  implements View.OnClickListener {
    private EditText rankTextET;
    private ImageButton rankAddBT;
    private LinearLayout container;
    private String TAG = "ROW";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup ViewContainer, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_new, ViewContainer, false);

        container = (LinearLayout)view.findViewById(R.id.container);
        rankTextET = (EditText)view.findViewById(R.id.rankTextET);
        rankAddBT = (ImageButton) view.findViewById(R.id.rankAddBT);
        rankAddBT.setOnClickListener(this);


        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: AddFragment");
        TextView rowTV;
        SharedPreferences preferences = getActivity().getSharedPreferences("realRank", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();

        for (int i = 0 ; i < container.getChildCount() ; i++){
            rowTV = (TextView)container.getChildAt(i).findViewById(R.id.rowTV);
            editor.putString(TAG+i,rowTV.getText().toString());
        }
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: AddFragment");

        container.removeAllViews();

        SharedPreferences preferences = getActivity()
                .getSharedPreferences("realRank", Activity.MODE_PRIVATE);
        Map<String, ?> memoryMap = preferences.getAll();
        for (Map.Entry<String,?> entry : memoryMap.entrySet()){
            addMemoryRows(entry.getValue().toString());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void addMemoryRows(String rowText){
        LayoutInflater layoutInflater = (LayoutInflater)
                getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View addView = layoutInflater.inflate(R.layout.add_row, null);

        TextView rowTV = (TextView)addView.findViewById(R.id.rowTV);
        ImageButton rowBT = (ImageButton)addView.findViewById(R.id.rowBT);

        rowTV.setText(rowText);
        rowBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LinearLayout)addView.getParent()).removeView(addView);
            }
        });
        container.addView(addView);
        rankTextET.setText("");
        rankTextET.requestFocus();
    }

    @Override
    public void onClick(View v) {
        if (!"".equalsIgnoreCase(rankTextET.getText().toString())) {
            addMemoryRows(rankTextET.getText().toString());
        }
    }
}





