package com.law.booking.activity.tools.Model;

import java.util.Date;

public class Schedule {
    public String time;
    private String name;
    private String imageUrl;
    private Date date;
    private String key;
    private String timeframe;
    private String description;
    private String address;
    private String userId;
    private String snapshotkey;
    public Schedule() {
    }

    public Schedule(String name, String imageUrl, Date date) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    public String getSnapshotkey() {
        return snapshotkey;
    }

    public void setSnapshotkey(String snapshotkey) {
        this.snapshotkey = snapshotkey;
    }

    public String getDescription() {
        return description;
    }

    public String getUserId() {
        return userId;
    }

    public String getAddress() {
        return address;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Date getDate() {
        return date;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
