package com.law.booking.activity.tools.Model;

import java.util.Date;

public class Schedule2 {
    private String time;
    private String name;
    private String imageUrl;
    private Date date;
    private String key;
    private String timeframe;
    private String userId;
    private boolean timeframeset;
    public Schedule2() {
    }

    public Schedule2(String name, String imageUrl, Date date, String key,String time,String timeframe) { // Include key in constructor
        this.name = name;
        this.imageUrl = imageUrl;
        this.date = date;
        this.key = key;
        this.time = time;
        this.timeframe = timeframe;
    }

    // Getters

    public boolean isTimeframeset() {
        return timeframeset;
    }

    public String getUserId() {
        return userId;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
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

    public String getKey() {
        return key;
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

    public void setKey(String key) {
        this.key = key;
    }
}
