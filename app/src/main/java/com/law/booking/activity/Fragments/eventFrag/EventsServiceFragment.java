package com.law.booking.activity.Fragments.eventFrag;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.law.booking.activity.MainPageActivity.Guess.package_event;
import com.law.booking.activity.tools.Model.Schedule3;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.adapter.ScheduleAdapter4;
import com.law.booking.activity.tools.adapter.portfolio_eventAdapter;
import com.law.booking.activity.tools.adapter.profileServiceAdapters;
import com.law.booking.R;

import java.util.ArrayList;
import java.util.List;


public class EventsServiceFragment extends Fragment implements profileServiceAdapters.OnServiceCheckedListener {
    private DatabaseReference serviceRef, guessRef,admin;
    private DatabaseReference portfoilio,Mysched;
    private profileServiceAdapters serviceAdapter;
    private ViewSkeletonScreen skeletonScreen;
    private portfolio_eventAdapter serviceAdapter2;
    private LinearLayout ll_skeleton;
    private RelativeLayout rl;
    private TextView numnber,total,sched,services;
    private RecyclerView myserviceRecycler,portfolioRecycler,schedulerecycler;
    private NestedScrollView scroller;
    private ImageView addtion,subraction;
    private AppCompatButton proceed;
    private boolean isAnyServiceChecked = false;
    String key = SPUtils.getInstance().getString(AppConstans.providers);
    private String TAG = "ServiceProvidersFragment";
    private String username,age,address,lengthofservice,image,email,phonenumber;
    private boolean isOnline;
    private String serviceName;
    private int currentNumber = 0;
    private double servicePrice = 0;
    private int totalPrice = 0;
    private List<Schedule3> scheduleList = new ArrayList<>();
    private ScheduleAdapter4 scheduleAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_providers, container, false);
        ll_skeleton = view.findViewById(R.id.ll_skeleton);
        ll_skeleton.setVisibility(View.VISIBLE);
        proceed = view.findViewById(R.id.proceed);
        services = view.findViewById(R.id.services);
        sched = view.findViewById(R.id.sched);
        scroller = view.findViewById(R.id.scroller);
        addtion = view.findViewById(R.id.addition);
        total = view.findViewById(R.id.total);
        rl = view.findViewById(R.id.rl);
        subraction = view.findViewById(R.id.subtraction);
        numnber = view.findViewById(R.id.number);
        proceed.setVisibility(View.GONE);
        myserviceRecycler = view.findViewById(R.id.priceList);
        portfolioRecycler = view.findViewById(R.id.portfolioRecycler);
        schedulerecycler = view.findViewById(R.id.schedulerecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        portfolioRecycler.setLayoutManager(layoutManager);
        myserviceRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        initSkeleton();
        fetchServices2();
        initGuessData();
        initAdminData();
        setupAddSubtractListeners();
        fetchSchedules();
        initClear();
        return view;
    }

    private void setupAddSubtractListeners() {
        addtion.setOnClickListener(v -> {
            currentNumber++;
            numnber.setText(String.valueOf(currentNumber));
            updateprice(currentNumber);
        });

        subraction.setOnClickListener(v -> {
            if (currentNumber > 0) {
                currentNumber--;
                numnber.setText(String.valueOf(currentNumber));
                updateprice(currentNumber);
            }
        });
    }

    private void updateprice(int currentNumber){
        double totalPrice = currentNumber * servicePrice;
        Log.d(TAG,"Price: "+totalPrice);
        total.setText("Total: "+totalPrice);
        SPUtils.getInstance().put(AppConstans.heads, String.valueOf(currentNumber));
        SPUtils.getInstance().put(AppConstans.pricetag, String.valueOf(totalPrice));
    }


    private void fetchServices2() {
        serviceRef = FirebaseDatabase.getInstance().getReference("EventOrg").child(key);
        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    services.add(service);

                }
                serviceAdapter2 = new portfolio_eventAdapter(services, getContext());
                portfolioRecycler.setAdapter(serviceAdapter2);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });
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
                scheduleAdapter = new ScheduleAdapter4(schedule3s, getContext());
                schedulerecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                schedulerecycler.setAdapter(scheduleAdapter);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching schedules: " + databaseError.getMessage());
            }
        });
    }

    private void initClear() {
        SPUtils spUtils = SPUtils.getInstance();
        String heads = AppConstans.heads;
        spUtils.remove(heads);
    }

    private void initAdminData() {
        admin = FirebaseDatabase.getInstance().getReference("ADMIN").child(key);
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

    private void fetchServices() {
        serviceRef = FirebaseDatabase.getInstance().getReference("EventOrg").child(key);
        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Service> services = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Service service = snapshot.getValue(Service.class);
                    if (service != null) {
                        services.add(service);
                    }
                }
                serviceAdapter = new profileServiceAdapters(services, EventsServiceFragment.this, getContext());
                myserviceRecycler.setAdapter(serviceAdapter);
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
            portfolioRecycler.setVisibility(View.VISIBLE);
            myserviceRecycler.setVisibility(View.VISIBLE);
            sched.setVisibility(View.VISIBLE);
            services.setVisibility(View.VISIBLE);
            scroller.setVisibility(View.VISIBLE);
            schedulerecycler.setVisibility(View.VISIBLE);
            ll_skeleton.setVisibility(View.GONE);
        }, 1000);
    }

    @Override
    public void onServiceChecked(boolean isChecked,double servicePrice2) {
        if (isChecked) {
            Log.d(TAG,"Price: "+servicePrice2);
            servicePrice = servicePrice2;
            proceed.setVisibility(View.VISIBLE);
            total.setVisibility(View.VISIBLE);
        } else {
            proceed.setVisibility(View.GONE);
            total.setVisibility(View.GONE);
            total.setText("0");
            numnber.setText("0");
        }
        isAnyServiceChecked = checkIfAnyServiceIsChecked();
        if (isAnyServiceChecked) {
            proceed.setVisibility(View.VISIBLE);
            rl.setVisibility(View.VISIBLE);
            proceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String price = SPUtils.getInstance().getString(AppConstans.pricetag);
                    String heads = SPUtils.getInstance().getString(AppConstans.heads);
                    if (heads == null || heads.isEmpty() || heads.equals("0")) {
                        Toast.makeText(getContext(), "Please add heads to continue.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(getContext(), package_event.class);
                    intent.putExtra("email",email);
                    intent.putExtra("username", username);
                    intent.putExtra("image", image);
                    intent.putExtra("address", address);
                    intent.putExtra("age", age);
                    intent.putExtra("lengthOfservice", lengthofservice);
                    intent.putExtra("key", key);
                    intent.putExtra("isOnline", isOnline);
                    intent.putExtra("serviceName", serviceName);
                    intent.putExtra("price",price);
                    intent.putExtra("heads",heads);
                    intent.putExtra("phonenumber",phonenumber);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }
            });
        } else {
            proceed.setVisibility(View.GONE);
            rl.setVisibility(View.GONE);
            total.setVisibility(View.GONE);
        }
    }
    private boolean checkIfAnyServiceIsChecked() {
        for (int i = 0; i < serviceAdapter.getItemCount(); i++) {
            Service service = serviceAdapter.getServiceList().get(i);
            if (service.isChecked()) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
