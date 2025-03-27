package com.law.booking.activity.tools.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.law.booking.activity.Fragments.AdminFrag.MyServicePriceFragment;
import com.law.booking.activity.Fragments.AdminFrag.Portfolio_dashboard;
import com.law.booking.activity.Fragments.AdminFrag.Review_adminfrag;


public class Admin_dashboard_adapter extends FragmentPagerAdapter {

    public Admin_dashboard_adapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Portfolio_dashboard();
            case 1:
                return new MyServicePriceFragment();
            case 2:
                return new Review_adminfrag();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
