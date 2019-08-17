package com.suncaption.realtimesearch;

import android.graphics.drawable.Drawable;

public class RRListItem {
    private String rank;
    private String title;
    private int upDown;
    private String upDownCnt;
    private Drawable upDownImg;
    private String url;
    private String urlChart;

    public String getRank() { return rank;    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUpDown() {
        return upDown;
    }

    public void setUpDown(int upDown) {
        this.upDown = upDown;
    }

    public String getUpDownCnt() {
        return upDownCnt;
    }

    public void setUpDownCnt(String upDownCnt) {
        this.upDownCnt = upDownCnt;
    }

    public Drawable getUpdownImg() {
        return upDownImg;
    }

    public void setUpdownImg(Drawable upDownImg) {
        this.upDownImg = upDownImg;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlChart() {
        return urlChart;
    }

    public void setUrlChart(String urlChart) {
        this.urlChart = urlChart;
    }
}
