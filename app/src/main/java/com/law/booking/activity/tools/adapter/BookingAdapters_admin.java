package com.law.booking.activity.tools.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.law.booking.activity.Fragments.AdminFrag.CancelFragment_admin;
import com.law.booking.activity.Fragments.AdminFrag.CompleteFragment;
import com.law.booking.activity.Fragments.AdminFrag.Confirmed_fragmentAdmin;
import com.law.booking.activity.Fragments.AdminFrag.HistoryBookFragment_admin;

public class BookingAdapters_admin extends FragmentPagerAdapter {
    public BookingAdapters_admin(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new HistoryBookFragment_admin();
                break;
            case 1:
                fragment = new Confirmed_fragmentAdmin();
                break;
            case 2:
                fragment = new CancelFragment_admin();
                break;
            case 3:
                fragment = new CompleteFragment();

                break;

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 4; // Number of fragments
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
