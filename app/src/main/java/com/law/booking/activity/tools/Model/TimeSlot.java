package com.law.booking.activity.tools.Model;

public class TimeSlot {
    private String time;
    private String userId;
    private String timevalue;
    private String key;
    private String date;


    public TimeSlot(){

    }
    // Constructor
    public TimeSlot(String time, String userId, String timevalue,String key,String date) {
        this.time = time;
        this.userId = userId;
        this.timevalue = timevalue;
        this.key = key;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimevalue() {
        return timevalue;
    }

    public void setTimevalue(String timevalue) {
        this.timevalue = timevalue;
    }
}
