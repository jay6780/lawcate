package com.law.booking.activity.Fragments.AdminFrag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.law.booking.R;
import com.law.booking.activity.tools.adapter.BookingAdapters_admin;

import java.util.ArrayList;

public class Admin_homedashboard extends Fragment {
    private CommonTabLayout dashboardtab;
    private ViewPager viewPager;
    private String[] mTitles;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_homedashboard, container, false);
        dashboardtab = view.findViewById(R.id.dashboard_tab);
        viewPager = view.findViewById(R.id.viewPager);
        BookingAdapters_admin adapter = new BookingAdapters_admin(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        InitTitle();
        initFragment();

        return view;
    }

    private void initFragment() {
        ArrayList<CustomTabEntity> list = new ArrayList<>();
        for (int i = 0; i < mTitles.length; i++) {
            final int j = i;
            list.add(new CustomTabEntity() {
                @Override
                public String getTabTitle() {
                    return mTitles[j];
                }

                @Override
                public int getTabSelectedIcon() {
                    return 0;
                }

                @Override
                public int getTabUnselectedIcon() {
                    return 0;
                }
            });
        }

        dashboardtab.setTabData(list);

        dashboardtab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);

            }

            @Override
            public void onTabReselect(int position) {
                if (position == 0 ) {
                }

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {
                dashboardtab.setCurrentTab(position);

            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void InitTitle() {
        mTitles = new String[]{
                "Schedule","Confirmation","Cancel","Completed"

        };
    }
    @Override
    public void onResume() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onResume();
    }
}