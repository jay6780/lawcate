package com.law.booking.activity.tools.Model;

public class Law_names {
    boolean isOpen;

    String law_name;

    public Law_names(String law_name,boolean isOpen){
        this.law_name = law_name;
        this.isOpen = isOpen;

    }

    public String getLaw_name() {
        return law_name;
    }

    public boolean isOpen() {
        return isOpen;
    }
}
