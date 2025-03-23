package com.law.booking.activity.MainPageActivity.bookingUi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.ViewSkeletonScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.Guess.Time_activity;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.Model.Usermodel;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.adapter.profileServiceAdapters2;

import java.util.ArrayList;
import java.util.List;


public class Service_lisfragment extends Fragment implements profileServiceAdapters2.OnServiceCheckedListener {
    private DatabaseReference serviceRef;
    private profileServiceAdapters2 serviceAdapter;
    private ViewSkeletonScreen skeletonScreen;
    private LinearLayout ll_skeleton;
    private RecyclerView myserviceRecycler;
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
    private Long bookcount;
    private String chatRoomId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_servicelist, container, false);
        ll_skeleton = view.findViewById(R.id.ll_skeleton);
        ll_skeleton.setVisibility(View.VISIBLE);
        proceed = view.findViewById(R.id.proceed);
        myserviceRecycler = view.findViewById(R.id.priceList);
        myserviceRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        initSkeleton();
        initAdminData();
        initClear();
        initbookcount();
        fetchServices();
        return view;

    }

    private void initbookcount() {
        DatabaseReference adminref = FirebaseDatabase.getInstance().getReference("Lawyer").child(key);
        adminref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    bookcount = dataSnapshot.child("bookcount").getValue(Long.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching EventOrg data: " + databaseError.getMessage());
            }
        });
    }
    private void initClear() {
        SPUtils spUtils = SPUtils.getInstance();
        String heads = AppConstans.heads;
        spUtils.remove(heads);
    }

    private void initAdminData() {
       DatabaseReference admin = FirebaseDatabase.getInstance().getReference("Lawyer").child(key);
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
                        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        chatRoomId = createChatRoomId(currentUserEmail, email);
                        SavedAta(chatRoomId,username,image,email,address,key,isOnline,phonenumber,age);
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

    private void SavedAta(String chatRoomId, String username, String image, String email, String address, String key, boolean isOnline, String phonenumber, String age) {
        SPUtils.getInstance().put(AppConstans.ChatRoomId, chatRoomId);
        SPUtils.getInstance().put(AppConstans.providerName, username);
        SPUtils.getInstance().put(AppConstans.image, image);
        SPUtils.getInstance().put(AppConstans.providerEmail, email);
        SPUtils.getInstance().put(AppConstans.address, address);
        SPUtils.getInstance().put(AppConstans.key, key);
        SPUtils.getInstance().put(AppConstans.isOnline, isOnline);
        SPUtils.getInstance().put(AppConstans.phone, phonenumber);
        SPUtils.getInstance().put(AppConstans.age, age);
    }

    private String createChatRoomId(String email1, String email2) {
        chatRoomId = email1.compareTo(email2) < 0
                ? email1.replace(".", "_") + "_" + email2.replace(".", "_")
                : email2.replace(".", "_") + "_" + email1.replace(".", "_");

        SPUtils.getInstance().put(AppConstans.ChatRoomId, chatRoomId);

        return chatRoomId;
    }

    
    private void fetchServices() {
        serviceRef = FirebaseDatabase.getInstance().getReference("Service").child(key);
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
                serviceAdapter = new profileServiceAdapters2(services, Service_lisfragment.this, getContext());
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
            myserviceRecycler.setVisibility(View.VISIBLE);
            ll_skeleton.setVisibility(View.GONE);
        }, 1000);
    }
    @Override
    public void onServiceChecked(boolean isChecked) {
        if (isChecked) {
            proceed.setVisibility(View.VISIBLE);
        } else {
            proceed.setVisibility(View.GONE);
        }
        initVisibility();
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String price = SPUtils.getInstance().getString(AppConstans.pricetag);
                serviceRef = FirebaseDatabase.getInstance().getReference("Service").child(key);
                serviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            navigateToBookNow(price, "", String.valueOf(bookcount));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error fetching Service data: " + databaseError.getMessage());
                    }
                });
            }
        });
    }

    private void initVisibility() {
        isAnyServiceChecked = checkIfAnyServiceIsChecked();
        if (isAnyServiceChecked) {
            proceed.setVisibility(View.VISIBLE);
        } else {
            proceed.setVisibility(View.GONE);
        }
    }

    private void navigateToBookNow(String price, String heads,String bookcount) {
        Intent intent = new Intent(getContext(), Time_activity.class);
        intent.putExtra("email", email);
        intent.putExtra("hmua",true);
        intent.putExtra("image_service",SPUtils.getInstance().getString(AppConstans.imageService));
        intent.putExtra("username", username);
        intent.putExtra("image", image);
        intent.putExtra("address", address);
        intent.putExtra("age", age);
        intent.putExtra("lengthOfservice", bookcount);
        intent.putExtra("key", key);
        intent.putExtra("isOnline", isOnline);
        intent.putExtra("serviceName", SPUtils.getInstance().getString(AppConstans.servicename));
        intent.putExtra("price", price);
        intent.putExtra("heads", heads);
        intent.putExtra("phonenumber", phonenumber);
        startActivity(intent);
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
