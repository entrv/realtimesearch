package com.suncaption.realtimesearch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baoyz.widget.PullRefreshLayout;

public class NaverFragment extends Fragment implements AdapterView.OnItemClickListener, RRListAdapter.ChartURLListener{
    private RRListAdapter naverAdapter;
    private ListView listView;
    private PullRefreshLayout naverRefresh;
    private ProgressDialog dialog;
    private TextView empty;

    public NaverFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_naver, container, false);

        naverAdapter = new RRListAdapter(this);
        empty = (TextView)rootView.findViewById(R.id.empty);
        listView = (ListView)rootView.findViewById(R.id.listview);
        listView.setAdapter(naverAdapter);
        listView.setOnItemClickListener(this);
        listView.setEmptyView(empty);

        naverRefresh = (PullRefreshLayout)rootView.findViewById(R.id.naverRefresh);
        naverRefresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dialog = new ProgressDialog(getContext());
                dialog.setMessage(getContext().getString(R.string.dialog_text));
                dialog.setCancelable(false);
                dialog.show();
                new RRAsyncTask(getContext(), naverAdapter, RRAsyncTask.NAVER_SITE,
                        new AsyncTaskCallBack() {
                    @Override
                    public void onSuccess() {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }).execute();
                naverRefresh.setRefreshing(false);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dialog = new ProgressDialog(getContext());
        dialog.setMessage(getContext().getString(R.string.dialog_text));
        dialog.setCancelable(false);
        dialog.show();
        new RRAsyncTask(getContext(), naverAdapter, RRAsyncTask.NAVER_SITE, new AsyncTaskCallBack() {
            @Override
            public void onSuccess() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }).execute();
    }

    @Override
    public void onURLclickListener(RRListItem item) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrlChart()));
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RRListItem item = (RRListItem)listView.getItemAtPosition(position);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl()));
        startActivity(intent);
    }
}
