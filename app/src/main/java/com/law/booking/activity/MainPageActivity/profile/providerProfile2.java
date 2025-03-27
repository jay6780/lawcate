package com.law.booking.activity.MainPageActivity.profile;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.bookingUi.history_book;
import com.law.booking.activity.MainPageActivity.chat.User_list;
import com.law.booking.activity.MainPageActivity.chat.chatActivity;
import com.law.booking.activity.MainPageActivity.maps.MapsActivity2;
import com.law.booking.activity.tools.Model.ChatRoom;
import com.law.booking.activity.tools.Model.Service;
import com.law.booking.activity.tools.Service.MessageNotificationService;
import com.law.booking.activity.tools.Utils.AppConstans;
import com.law.booking.activity.tools.Utils.SPUtils;
import com.law.booking.activity.tools.Utils.Utils;
import com.law.booking.activity.tools.adapter.ServiceProviderAdapter;
import com.law.booking.activity.tools.adapter.coverAdapter_user;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class providerProfile2 extends AppCompatActivity {
    private TextView userAddress,name,profiletxt;
    private ImageView profileimage,backBtn,bell,messageImg;
    CardView messagebtn;
    private String chatRoomId;
    private String image;
    private String description;
    private String email;
    private String[] mTitles;
    private String providerName;
    private String address,key;
    private String age,lengthOfservice;
    private String TAG = "providerProfile2";
    private boolean isOnline;
    private DatabaseReference databaseReference;
    private TextView description_view;
    private Banner banner;
    private CommonTabLayout hmua_tab;
    String bookprovideremail = SPUtils.getInstance().getString(AppConstans.bookprovideremail);
    private ViewPager viewPager;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lawyer_profile_user);
        changeStatusBarColor(getResources().getColor(R.color.white2));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        viewPager = findViewById(R.id.viewPager);
        ServiceProviderAdapter adapter = new ServiceProviderAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        hmua_tab = findViewById(R.id.hmua_tab);
        profiletxt = findViewById(R.id.profiletxt);
        description_view = findViewById(R.id.description_view);
        banner = findViewById(R.id.banner);
        profileimage = findViewById(R.id.avatar);
        name = findViewById(R.id.name);
        bell = findViewById(R.id.bell);
        backBtn = findViewById(R.id.back);
        messageImg = findViewById(R.id.messageImg);
        userAddress = findViewById(R.id.address);
        messagebtn = findViewById(R.id.message);
        key = getIntent().getStringExtra("key");
        profiletxt.setText("Lawyer details");
        profiletxt.setTextSize(18);
        isOnline = getIntent().getBooleanExtra("isOnline", false);
        Log.d(TAG, "Is Online: " + isOnline);
        SPUtils.getInstance().put(AppConstans.KEY, key);
        Log.d("SavedKey", "userId: " + key);
        if (key == null) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            Intent redirect = new Intent(getApplicationContext(), User_list.class);
            startActivity(redirect);
            overridePendingTransition(0,0);
            finish(); // or redirect
            return;
        }
        String location = getString(R.string.address);
        String names = getString(R.string.name);
        String taon = getString(R.string.yearss);




        description = getIntent().getStringExtra("description");
        image = getIntent().getStringExtra("image");
        SPUtils.getInstance().put(AppConstans.image_profile,image);
        providerName = getIntent().getStringExtra("username");
        SPUtils.getInstance().put(AppConstans.profilename,providerName);
        address = getIntent().getStringExtra("address");
        email = getIntent().getStringExtra("email");
        age = getIntent().getStringExtra("age");
        lengthOfservice = getIntent().getStringExtra("lengthOfservice");
        SPUtils.getInstance().put(AppConstans.bookcount,lengthOfservice);
        Log.d(TAG, "Storing Provider Name: " + providerName);
        SPUtils.getInstance().put(AppConstans.providerName, providerName);
        Log.d(TAG, "Storing Email: " + email);
        SPUtils.getInstance().put(AppConstans.providers, key);
        SPUtils.getInstance().put(AppConstans.email, email);
        name.setText((providerName != null ? providerName : "N/A"));
        userAddress.setText((address != null ? address : "N/A"));
        messageImg.setOnClickListener(view -> chat());
        if(description !=null){
            description_view.setText(description);
        }else{
            description_view.setVisibility(View.GONE);
        }

        savedataprofile(providerName,image,email,address,key,isOnline,age,lengthOfservice,description);

        Glide.with(this)
                .load(image)
                .transform(new CircleCrop())
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_person_24)
                .into(profileimage);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity2.class);
                intent.putExtra("username", providerName);
                intent.putExtra("address",address);
                intent.putExtra("age", age);
                intent.putExtra("lengthOfservice",lengthOfservice);
                intent.putExtra("image",image);
                intent.putExtra("email",email);
                startActivity(intent);
                finish();
            }
        });
        InitTitle();
        initFragment();
        initShowbook();
        fetchServices();
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



        messagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndCreateChatRoom(email,providerName,image);
            }
        });

    }

    private void savedataprofile(String providerName, String image, String email, String address, String key, boolean isOnline,String age,String service_length,String description) {
        SPUtils.getInstance().put(AppConstans.providerName, providerName);
        SPUtils.getInstance().put(AppConstans.image, image);
        SPUtils.getInstance().put(AppConstans.providerEmail, email);
        SPUtils.getInstance().put(AppConstans.address, address);
        SPUtils.getInstance().put(AppConstans.key, key);
        SPUtils.getInstance().put(AppConstans.isOnline, isOnline);
        SPUtils.getInstance().put(AppConstans.provider_age, age);
        SPUtils.getInstance().put(AppConstans.service_length, service_length);
        SPUtils.getInstance().put(AppConstans.description, description);


    }

    private void InitTitle() {
        mTitles = new String[]{
                "Portfolio","Services","Reviews"

        };
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

        hmua_tab.setTabData(list);

        hmua_tab.setOnTabSelectListener(new OnTabSelectListener() {
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
                hmua_tab.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void fetchServices() {
        DatabaseReference serviceRef = FirebaseDatabase.getInstance().getReference("Cover_photo").child(key);
        serviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Service> services = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Service service = snapshot.getValue(Service.class);
                        String image = service.getImageUrl();
                        Log.d("Banners","image: "+image);
                        services.add(service);
                        setupBanner(services);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching services: " + databaseError.getMessage());
            }
        });
    }

    private void setupBanner(List<Service> services) {
        if (providerProfile2.this != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            providerProfile2.this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int heightInPixels = Utils.dp2px(providerProfile2.this, 200);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(screenWidth, heightInPixels);
            banner.setLayoutParams(lp);
            banner.setAdapter(new coverAdapter_user(services, providerProfile2.this))
                    .setIndicator(new CircleIndicator(providerProfile2.this))
                    .setOnBannerListener((data, position) -> {
                        // Handle banner click events
                    })
                    .start();
        }
    }

    private void chat() {
        Intent intent = new Intent(getApplicationContext(), User_list.class);
        startActivity(intent);
    }
    private void initShowbook() {
        startService(new Intent(this, MessageNotificationService.class));
        TextView badgeCount = findViewById(R.id.badge_count);
        String badgenum = SPUtils.getInstance().getString(AppConstans.booknum);
        if(badgenum.isEmpty() || badgenum.equals("null")){
            SPUtils.getInstance().put(AppConstans.booknum, "0");
        }else{
            badgeCount.setVisibility(View.VISIBLE);
            badgeCount.setText(badgenum);

        }
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), history_book.class);
                intent.putExtra("bookprovideremail", bookprovideremail);
                startActivity(intent);
            }
        });

    }


    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    private void checkAndCreateChatRoom(String providerEmail, String providerName,String image) {
        String currentUserEmail = SPUtils.getInstance().getString(AppConstans.userEmail);
        String chatRoomId = createChatRoomId(currentUserEmail, providerEmail);
        databaseReference.child("chatRooms").child(chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    ChatRoom chatRoom = new ChatRoom();
                    chatRoom.setUsers(Arrays.asList(currentUserEmail, providerEmail));
                    databaseReference.child("chatRooms").child(chatRoomId).setValue(chatRoom)
                            .addOnSuccessListener(aVoid -> {
                                Intent intent = new Intent(getApplicationContext(), chatActivity.class);
                                intent.putExtra("chatRoomId", chatRoomId);
                                intent.putExtra("providerName", providerName);
                                intent.putExtra("image",image);
                                intent.putExtra("providerEmail",providerEmail);
                                intent.putExtra("address",address);
                                intent.putExtra("key",key);
                                intent.putExtra("isOnline",isOnline);
                                SavedAta(chatRoomId,providerName,image,providerEmail,address,key,isOnline);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Failed to create chat room", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Intent intent = new Intent(getApplicationContext(), chatActivity.class);
                    intent.putExtra("chatRoomId", chatRoomId);
                    intent.putExtra("providerName", providerName);
                    intent.putExtra("image",image);
                    intent.putExtra("providerEmail",providerEmail);
                    intent.putExtra("address",address);
                    intent.putExtra("key",key);
                    intent.putExtra("isOnline",isOnline);
                    SavedAta(chatRoomId,providerName,image,providerEmail,address,key,isOnline);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to check chat room", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SavedAta(String chatRoomId, String providerName, String image, String providerEmail, String address, String key, boolean isOnline) {
        SPUtils.getInstance().put(AppConstans.ChatRoomId, chatRoomId);
        SPUtils.getInstance().put(AppConstans.providerName, providerName);
        SPUtils.getInstance().put(AppConstans.image, image);
        SPUtils.getInstance().put(AppConstans.providerEmail, providerEmail);
        SPUtils.getInstance().put(AppConstans.address, address);
        SPUtils.getInstance().put(AppConstans.key, key);
        SPUtils.getInstance().put(AppConstans.isOnline, isOnline);
    }

    private String createChatRoomId(String email1, String email2) {
        chatRoomId = email1.compareTo(email2) < 0
                ? email1.replace(".", "_") + "_" + email2.replace(".", "_")
                : email2.replace(".", "_") + "_" + email1.replace(".", "_");

        SPUtils.getInstance().put(AppConstans.ChatRoomId, chatRoomId);

        return chatRoomId;
    }
    public void onBackPressed(){
        finish();
        super.onBackPressed();
    }
}