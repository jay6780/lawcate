package com.law.booking.activity.Fragments.AdminFrag;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.Admin.UserChat;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.Utils.Utils;
import com.law.booking.activity.tools.adapter.Admin_dashboard_adapter;
import com.law.booking.activity.tools.adapter.coverAdapter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

public class Admin_dashboard_fragment extends Fragment implements OnRefreshListener {
    private String TAG ="Admin_dashboard_fragment";
    private String[] mTitles;
    private CommonTabLayout dashboardtab;
    private ViewPager viewPager;
    private Banner banner;
    private ImageView avatar;
    private CardView message;
    private TextView name,address,description_view;
    private SmartRefreshLayout refreshLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         getActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        View view = inflater.inflate(R.layout.fragment_admin_dashboard_fragment, container, false);
        dashboardtab = view.findViewById(R.id.dashboard_tab);
        viewPager = view.findViewById(R.id.viewPager);
        name = view.findViewById(R.id.name);
        avatar = view.findViewById(R.id.avatar);
        message = view.findViewById(R.id.message);
        address = view.findViewById(R.id.address);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        description_view = view.findViewById(R.id.description_view);
        banner = view.findViewById(R.id.banner);
        Admin_dashboard_adapter adapter = new Admin_dashboard_adapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnableRefresh(true);
        InitTitle();
        initFragment();
        fetchServices();
        initMyadata();
        message.setOnClickListener(view1 -> openchat());
        return view;

    }


    private void openchat() {
        Intent chat = new Intent(getActivity(), UserChat.class);
        getActivity().startActivity(chat);
        getActivity().overridePendingTransition(0,0);
        getActivity().finish();
    }

    private void initMyadata() {
        if (getContext() == null || !isAdded()) {
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Lawyer").child(userId);
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                String image = snapshot.child("image").getValue(String.class);
                String myname = snapshot.child("name").getValue(String.class);
                String description = snapshot.child("description").getValue(String.class);
                String Address = snapshot.child("address").getValue(String.class);

                if (myname != null && description != null && Address != null && image != null) {
                    Glide.with(requireContext())
                            .load(image)
                            .circleCrop()
                            .error(R.mipmap.man)
                            .into(avatar);

                    name.setText(myname);
                    address.setText(Address);
                    description_view.setText(description);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }


    private void fetchServices() {
        String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference serviceRef = FirebaseDatabase.getInstance().getReference("Cover_photo").child(key);
        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Service service = snapshot.getValue(Service.class);
                        if (service != null) {
                            services.add(service);
                        }
                    }
                }
                setupBanner(services);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });
    }


    private void setupBanner(List<Service> services) {
        if (getActivity() == null || banner == null) {
            return;
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int heightInPixels = Utils.dp2px(getContext(), 200);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(screenWidth, heightInPixels);
        banner.setLayoutParams(lp);
        banner.setAdapter(new coverAdapter(services, getContext()))
                .setIndicator(new CircleIndicator(getContext()))
                .setOnBannerListener((data, position) -> {})
                .start();
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
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

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
                "Portfolio","Services","Reviews"

        };
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.getLayout().postDelayed(() -> {
            fetchServices();
            initMyadata();
            refreshFragments();
            refreshLayout.finishRefresh();
        }, 100);
    }

    private void refreshFragments() {
            int currentPosition = viewPager.getCurrentItem();
            Admin_dashboard_adapter adapter = new Admin_dashboard_adapter(getChildFragmentManager());
            viewPager.setAdapter(adapter);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(currentPosition, false);
            dashboardtab.setCurrentTab(currentPosition);
        }

}