package com.law.booking.activity.Fragments.UserFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.ViewSkeletonScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.Schedule3;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.adapter.portfolioAdapter;
import com.law.booking.activity.tools.adapter.profileServiceAdapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ServiceProvidersFragment extends Fragment implements profileServiceAdapters.OnServiceCheckedListener {
    private DatabaseReference serviceRef, guessRef,admin,eventOrg,events;
    private DatabaseReference portfoilio,Mysched,portfolioevent;
    private portfolioAdapter portfolioAdapter;
    private ViewSkeletonScreen skeletonScreen;
    private LinearLayout ll_skeleton;
    private RecyclerView portfolioRecycler;
    private NestedScrollView scroller;
    private boolean isAnyServiceChecked = false;
    String key = SPUtils.getInstance().getString(AppConstans.providers);
    private String TAG = "ServiceProvidersFragment";
    private String username,age,address,lengthofservice,image,email,phonenumber;
    private boolean isOnline;
    private String serviceName;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.service_new_layout, container, false);
        ll_skeleton = view.findViewById(R.id.ll_skeleton);
        ll_skeleton.setVisibility(View.VISIBLE);
        scroller = view.findViewById(R.id.scroller);
        portfolioRecycler = view.findViewById(R.id.portfolioRecycler);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        portfolioRecycler.setLayoutManager(gridLayoutManager);
        initSkeleton();
        initGuessData();
        initAdminData();
        fetchSchedules();
        initClear();

        return view;
    }
    private void fetchSchedules() {
        Mysched = FirebaseDatabase.getInstance().getReference("Mysched").child(key);
        Mysched.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Schedule3> schedule3s = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Schedule3 schedule3 = snapshot.getValue(Schedule3.class);
                    if (schedule3 != null) {
                        schedule3s.add(schedule3);
                        Log.d(TAG, "Time: " + schedule3.getTime());
                        Log.d(TAG, "Date: " + schedule3.getDate());
                    }
                }

                Collections.sort(schedule3s, new Comparator<Schedule3>() {
                    @Override
                    public int compare(Schedule3 s1, Schedule3 s2) {
                        return s1.getDate().compareTo(s2.getDate());
                    }
                });

                if (schedule3s.isEmpty()) {
                    Log.d(TAG, "No schedules available.");
                    portfolioRecycler.setVisibility(View.GONE);
                } else {
                    portfolioRecycler.setVisibility(View.VISIBLE);

                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching schedules: " + databaseError.getMessage());
            }
        });
    }

    private void layout_position() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.setMargins(0, 300, 0, 0);

    }



    private void initClear() {
        SPUtils spUtils = SPUtils.getInstance();
        String heads = AppConstans.heads;
        spUtils.remove(heads);
    }

    private void initAdminData() {
        admin = FirebaseDatabase.getInstance().getReference("Lawyer").child(key);
        events = FirebaseDatabase.getInstance().getReference("ADMIN").child(key);
        admin.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usermodel user = dataSnapshot.getValue(Usermodel.class);
                    if (user != null) {
                        username = user.getUsername();
                        age = user.getAge();
                        address = user.getAddress();
                        lengthofservice = user.getLengthOfService();
                        image = user.getImage();
                        email = user.getEmail();
                        isOnline = user.isOnline();
                        phonenumber = user.getPhone();
                        Savedbooknowdata(username,age,address,lengthofservice,image,email,isOnline,phonenumber,key);
                    }
                }
            }
            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching user data: " + databaseError.getMessage());
            }
        });

        events.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usermodel user = dataSnapshot.getValue(Usermodel.class);
                    if (user != null) {
                        username = user.getUsername();
                        age = user.getAge();
                        address = user.getAddress();
                        lengthofservice = user.getLengthOfService();
                        image = user.getImage();
                        email = user.getEmail();
                        isOnline = user.isOnline();
                        phonenumber = user.getPhone();
                    }
                }
            }
            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching user data: " + databaseError.getMessage());
            }
        });
    }

    private void Savedbooknowdata(String username, String age, String address, String lengthofservice, String image, String email, boolean isOnline, String phonenumber,String key) {
        SPUtils.getInstance().put(AppConstans.AdminUsername,username);
        SPUtils.getInstance().put(AppConstans.AdminAge,age);
        SPUtils.getInstance().put(AppConstans.AdminAdress,address);
        SPUtils.getInstance().put(AppConstans.AdminLenght,lengthofservice);
        SPUtils.getInstance().put(AppConstans.AdminImage,image);
        SPUtils.getInstance().put(AppConstans.Adminemail,email);
        SPUtils.getInstance().put(AppConstans.AdminPhone,phonenumber);
        SPUtils.getInstance().put(AppConstans.AdminKey,key);
    }

    private void initGuessData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            guessRef = FirebaseDatabase.getInstance().getReference("Client").child(userId);
            guessRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Usermodel user = dataSnapshot.getValue(Usermodel.class);
                        if (user != null) {
                            fetchServices();
                            fetchPortfolio();
                        }
                    }
                }
                @SuppressLint("LongLogTag")
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error fetching user data: " + databaseError.getMessage());
                }
            });
        }
    }

    private void fetchPortfolio() {
        portfoilio = FirebaseDatabase.getInstance().getReference("Service").child(key);
        portfolioevent = FirebaseDatabase.getInstance().getReference("EventOrg").child(key);
        portfoilio.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    if (service != null) {
                        services.add(service);
                        fetchPortfoliorecycler(services);
                    }
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });


        portfolioevent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    if (service != null) {
                        services.add(service);
                        fetchPortfoliorecycler(services);
                    }
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });
    }

    private void fetchPortfoliorecycler(List<Service> services) {
        portfolioAdapter = new portfolioAdapter(services, getContext());
        portfolioRecycler.setAdapter(portfolioAdapter);
    }

    private void fetchServices() {
        serviceRef = FirebaseDatabase.getInstance().getReference("Service").child(key);
        eventOrg = FirebaseDatabase.getInstance().getReference("EventOrg").child(key);
        String discountServiceName = SPUtils.getInstance().getString(AppConstans.discountservicename);

        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                List<String> lawlist = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    if (service != null) {
                        String serviceName = service.getName();
                        lawlist.add(serviceName);
                        String lawlistjson = new Gson().toJson(lawlist);
                        Log.d("lawnames","value: "+lawlistjson);
                        SPUtils.getInstance().put(AppConstans.servicelist,lawlistjson);
                        if ((discountServiceName == null || discountServiceName.isEmpty() ||
                                        (serviceName != null && serviceName.equals(discountServiceName)))) {
                            services.add(service);
                        }
                    }
                }

            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });

        eventOrg.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    if (service != null) {
                        String serviceName = service.getName();
                        if ((discountServiceName == null || discountServiceName.isEmpty() ||
                                        (serviceName != null && serviceName.equals(discountServiceName)))
                                ||service.getGender() == null && service.getGender().isEmpty()) {
                            services.add(service);
                        }
                    }
                }

            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });
    }

    private void initSkeleton() {
        skeletonScreen = Skeleton.bind(ll_skeleton)
                .load(R.layout.skeletonlayout_2)
                .duration(1000)
                .color(R.color.colorFontGreyDark)
                .angle(20)
                .show();
        new Handler().postDelayed(() -> {
            skeletonScreen.hide();
            scroller.setVisibility(View.VISIBLE);
            ll_skeleton.setVisibility(View.GONE);
        }, 1000);
    }
    @Override
    public void onServiceChecked(boolean isChecked, double servicePrice2) {
        if (isChecked) {
            Log.d(TAG, "Price: " + servicePrice2);
        } else {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
