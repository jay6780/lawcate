package com.law.booking.activity.tools.Model;

import java.util.Date;

public class Schedule4 {
    public String time;
    private String name;
    private String imageUrl;
    private Date date;
    private String key;
    private String userId;
    public Schedule4() {
    }

    public Schedule4(String name, String imageUrl, Date date,String key,String userId) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.date = date;
        this.key = key;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
