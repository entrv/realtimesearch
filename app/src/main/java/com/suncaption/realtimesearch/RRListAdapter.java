package com.suncaption.realtimesearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RRListAdapter extends BaseAdapter {
    private ArrayList<RRListItem> listItemList = new ArrayList<>();
    private ChartURLListener listener;

    public RRListAdapter() { }

    public RRListAdapter(ChartURLListener listener) {
        this.listener = listener;
    }

    public ArrayList<RRListItem> getListItemList(){
        return listItemList;
    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public Object getItem(int i) {
        return listItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();
        ViewHolder holder;

        if (view != null) {
            holder = (ViewHolder)view.getTag();
        } else {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.rank_listview_item, null);
            holder.titleView = (TextView)view.findViewById(R.id.item_text);
            holder.upDownView = (TextView)view.findViewById(R.id.item_updown);
            holder.rankImage = (ImageView)view.findViewById(R.id.item_img);

            view.setTag(holder);
        }

        final RRListItem listItem = listItemList.get(i);
        holder.titleView.setText(listItem.getRank()+listItem.getTitle());
        holder.upDownView.setText(listItem.getUpDownCnt());
        holder.rankImage.setImageDrawable(listItem.getUpdownImg());
        holder.rankImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onURLclickListener(listItem);
                }
            }
        });

        return view;
    }

    public void addItem(RRListItem item){
        listItemList.add(item);
    }

    public interface ChartURLListener {
        void onURLclickListener(RRListItem item);
    }

    private class ViewHolder {
        private TextView titleView;
        private TextView upDownView;
        private ImageView rankImage;
    }
}